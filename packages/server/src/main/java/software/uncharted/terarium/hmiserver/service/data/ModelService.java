package software.uncharted.terarium.hmiserver.service.data;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.SourceConfig;
import co.elastic.clients.elasticsearch.core.search.SourceFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.observation.annotation.Observed;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelDescription;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelMetadata;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata.Annotations;
import software.uncharted.terarium.hmiserver.repository.data.ModelRepository;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.gollm.EmbeddingService;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;
import software.uncharted.terarium.hmiserver.service.tasks.TaskService;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@Slf4j
@Service
public class ModelService extends TerariumAssetServiceWithSearch<Model, ModelRepository> {

	private final CurrentUserService currentUserService;
	private final DocumentAssetService documentAssetService;
	private final TaskService taskService;
	private final DKGService dkgService;

	public ModelService(
		final Config config,
		final CurrentUserService currentUserService,
		final DocumentAssetService documentAssetService,
		final ElasticsearchConfiguration elasticConfig,
		final ElasticsearchService elasticService,
		final EmbeddingService embeddingService,
		final Environment env,
		final DKGService dkgService,
		final ModelRepository repository,
		final ObjectMapper objectMapper,
		final ProjectAssetService projectAssetService,
		final ProjectService projectService,
		final S3ClientService s3ClientService,
		final TaskService taskService
	) {
		super(
			objectMapper,
			config,
			elasticConfig,
			elasticService,
			embeddingService,
			env,
			projectService,
			projectAssetService,
			s3ClientService,
			repository,
			Model.class
		);
		this.currentUserService = currentUserService;
		this.documentAssetService = documentAssetService;
		this.taskService = taskService;
		this.dkgService = dkgService;
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
		return elasticConfig.getModelIndex();
	}

	@Override
	@Observed(name = "function_profile")
	protected String getAssetPath() {
		throw new UnsupportedOperationException("Models are not stored in S3");
	}

	@Override
	public String getAssetAlias() {
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
		asset
			.getParameters()
			.forEach(param -> {
				if (param.getValue() == null) {
					param.setValue(1.0);
				}
			});

		// Force proper annotation metadata
		final ModelMetadata metadata = asset.getMetadata();
		if (metadata.getAnnotations() == null) {
			metadata.setAnnotations(new Annotations());
			asset.setMetadata(metadata);
		}

		if (asset.getHeader() != null && asset.getHeader().getName() != null) {
			asset.setName(asset.getHeader().getName());
		}

		JsonNode time = asset.getSemantics().getOde().getTime();
		// set a time parameter and default to day for the model if it doesn't exist
		if (time == null || time.get("id") == null) {
			asset.getSemantics().getOde().setTime(objectMapper.createObjectNode().put("id", "t"));
		}

		// if there is a time parameter, set the default to date if it is "day"
		if (time != null && time.get("units") != null && time.get("units").get("expression").asText().equals("day")) {
			final String id = asset.getSemantics().getOde().getTime().get("id").asText();
			ObjectNode unitsNode = objectMapper
				.createObjectNode()
				.put("expression", "date")
				.put("expression_mathml", "<ci>date</ci>");
			ObjectNode timeNode = objectMapper.createObjectNode().put("id", id).set("units", unitsNode);
			asset.getSemantics().getOde().setTime(timeNode);
		}
		final Model created = super.createAsset(asset, projectId, hasWritePermission);
		generateAndUpsertEmbeddings(created);

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

		// Force proper annotation metadata
		final ModelMetadata metadata = asset.getMetadata();
		if (metadata.getAnnotations() == null) {
			metadata.setAnnotations(new Annotations());
			asset.setMetadata(metadata);
		}

		final Optional<Model> updatedOptional = super.updateAsset(asset, projectId, hasWritePermission);
		if (updatedOptional.isEmpty()) {
			return Optional.empty();
		}

		final Model updated = updatedOptional.get();
		generateAndUpsertEmbeddings(updated);

		return updatedOptional;
	}
}
