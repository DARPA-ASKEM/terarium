package software.uncharted.terarium.hmiserver.service.data;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.SourceConfig;
import co.elastic.clients.elasticsearch.core.search.SourceFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.TerariumAssetEmbeddings;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelDescription;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelMetadata;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata.Annotations;
import software.uncharted.terarium.hmiserver.repository.data.ModelRepository;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.gollm.EmbeddingService;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@Slf4j
@Service
public class ModelService extends TerariumAssetServiceWithSearch<Model, ModelRepository> {

	private final EmbeddingService embeddingService;

	private final Environment env;

	public ModelService(
		final ObjectMapper objectMapper,
		final Config config,
		final ElasticsearchConfiguration elasticConfig,
		final ElasticsearchService elasticService,
		final ProjectService projectService,
		final ProjectAssetService projectAssetService,
		final S3ClientService s3ClientService,
		final ModelRepository repository,
		final EmbeddingService embeddingService,
		final Environment env
	) {
		super(
			objectMapper,
			config,
			elasticConfig,
			elasticService,
			projectService,
			projectAssetService,
			s3ClientService,
			repository,
			Model.class
		);
		this.embeddingService = embeddingService;
		this.env = env;
	}

	private boolean isRunningTestProfile() {
		final String[] activeProfiles = env.getActiveProfiles();

		for (final String profile : activeProfiles) {
			if ("test".equals(profile)) {
				return true;
			}
		}

		return false;
	}

	@Observed(name = "function_profile")
	public List<ModelDescription> getDescriptions(final Integer page, final Integer pageSize) throws IOException {
		final SourceConfig source = new SourceConfig.Builder()
			.filter(new SourceFilter.Builder().excludes("model", "semantics").build())
			.build();

		final SearchRequest req = new SearchRequest.Builder()
			.index(getAssetAlias())
			.from(page)
			.size(pageSize)
			.query(q ->
				q.bool(b ->
					b
						.mustNot(mn -> mn.exists(e -> e.field("deletedOn")))
						.mustNot(mn -> mn.term(t -> t.field("temporary").value(true)))
						.mustNot(mn -> mn.term(t -> t.field("isPublic").value(false)))
				)
			)
			.source(source)
			.build();

		return elasticService.search(req, Model.class).stream().map(m -> ModelDescription.fromModel(m)).toList();
	}

	@Observed(name = "function_profile")
	public Optional<ModelDescription> getDescription(final UUID id, final Schema.Permission hasReadPermission)
		throws IOException {
		final Optional<Model> model = getAsset(id, hasReadPermission);
		if (model.isPresent()) {
			final ModelDescription md = ModelDescription.fromModel(model.get());
			return Optional.of(md);
		}

		return Optional.empty();
	}

	@Override
	@Observed(name = "function_profile")
	protected String getAssetIndex() {
		log.info("MODEL INDEX: {}", elasticConfig.getModelIndex());
		return elasticConfig.getModelIndex();
	}

	@Override
	@Observed(name = "function_profile")
	protected String getAssetPath() {
		throw new UnsupportedOperationException("Models are not stored in S3");
	}

	@Override
	public String getAssetAlias() {
		log.info("MODEL ALIAS: {}", elasticConfig.getModelAlias());
		return elasticConfig.getModelAlias();
	}

	@Observed(name = "function_profile")
	public Optional<Model> getModelFromModelConfigurationId(
		final UUID modelConfigurationId,
		final Schema.Permission hasReadPermission
	) {
		return repository.findModelByModelConfigurationId(modelConfigurationId);
	}

	@Override
	@Observed(name = "function_profile")
	public Model createAsset(final Model asset, final UUID projectId, final Schema.Permission hasWritePermission)
		throws IOException {
		// Make sure that the model framework is set to lowercase
		if (asset.getHeader() != null && asset.getHeader().getSchemaName() != null) asset
			.getHeader()
			.setSchemaName(asset.getHeader().getSchemaName().toLowerCase());

		// Set default value for model parameters (0.0)
		if (
			asset.getSemantics() != null &&
			asset.getSemantics().getOde() != null &&
			asset.getSemantics().getOde().getParameters() != null
		) {
			asset
				.getSemantics()
				.getOde()
				.getParameters()
				.forEach(param -> {
					if (param.getValue() == null) {
						param.setValue(1.0);
					}
				});
		}

		// Force observable to empty-list if null or not specified
		if (asset.getSemantics() != null) {
			if (asset.getSemantics().getOde().getObservables() == null) {
				asset.getSemantics().getOde().setObservables(new ArrayList());
			}
		}
		// Force proper annotation metadata
		ModelMetadata metadata = asset.getMetadata();
		if (metadata.getAnnotations() == null) {
			metadata.setAnnotations(new Annotations());
			asset.setMetadata(metadata);
		}

		if (asset.getHeader() != null && asset.getHeader().getName() != null) {
			asset.setName(asset.getHeader().getName());
		}
		final Model created = super.createAsset(asset, projectId, hasWritePermission);

		if (!isRunningTestProfile() && created.getPublicAsset() && !created.getTemporary()) {
			String text;
			if (created.getMetadata() != null && created.getMetadata().getGollmCard() != null) {
				text = objectMapper.writeValueAsString(created.getMetadata().getGollmCard());
			} else {
				text = objectMapper.writeValueAsString(created);
			}

			new Thread(() -> {
				try {
					final TerariumAssetEmbeddings embeddings = embeddingService.generateEmbeddings(text);

					// Execute the update request
					uploadEmbeddings(created.getId(), embeddings, hasWritePermission);
				} catch (final Exception e) {
					log.error("Failed to update embeddings for model {}", created.getId(), e);
				}
			}).start();
		}

		return created;
	}

	@Override
	@Observed(name = "function_profile")
	public Optional<Model> updateAsset(
		final Model asset,
		final UUID projectId,
		final Schema.Permission hasWritePermission
	) throws IOException, IllegalArgumentException {
		if (asset.getHeader() != null && asset.getHeader().getName() != null) {
			asset.setName(asset.getHeader().getName());
		}

		// Force observable to empty-list if null or not specified
		if (asset.getSemantics() != null) {
			if (asset.getSemantics().getOde().getObservables() == null) {
				asset.getSemantics().getOde().setObservables(new ArrayList());
			}
		}
		// Force proper annotation metadata
		ModelMetadata metadata = asset.getMetadata();
		if (metadata.getAnnotations() == null) {
			metadata.setAnnotations(new Annotations());
			asset.setMetadata(metadata);
		}

		final Optional<Model> updatedOptional = super.updateAsset(asset, projectId, hasWritePermission);
		if (updatedOptional.isEmpty()) {
			return Optional.empty();
		}

		final Model updated = updatedOptional.get();

		if (!isRunningTestProfile() && updated.getPublicAsset() && !updated.getTemporary()) {
			String text;
			if (updated.getMetadata() != null && updated.getMetadata().getGollmCard() != null) {
				text = objectMapper.writeValueAsString(updated.getMetadata().getGollmCard());
			} else {
				text = objectMapper.writeValueAsString(updated);
			}

			new Thread(() -> {
				try {
					final TerariumAssetEmbeddings embeddings = embeddingService.generateEmbeddings(text);

					// Execute the update request
					uploadEmbeddings(updated.getId(), embeddings, hasWritePermission);
				} catch (final Exception e) {
					log.error("Failed to update embeddings for model {}", updated.getId(), e);
				}
			}).start();
		}

		return updatedOptional;
	}
}
