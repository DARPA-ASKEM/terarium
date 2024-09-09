package software.uncharted.terarium.hmiserver.service.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.TerariumAssetEmbeddings;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.repository.data.DocumentRepository;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.gollm.EmbeddingService;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@Slf4j
@Service
public class DocumentAssetService extends TerariumAssetServiceWithSearch<DocumentAsset, DocumentRepository> {

	private final EmbeddingService embeddingService;

	private final Environment env;

	public DocumentAssetService(
		final ObjectMapper objectMapper,
		final Config config,
		final ElasticsearchConfiguration elasticConfig,
		final ElasticsearchService elasticService,
		final ProjectService projectService,
		final ProjectAssetService projectAssetService,
		final S3ClientService s3ClientService,
		final DocumentRepository repository,
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
			DocumentAsset.class
		);
		this.embeddingService = embeddingService;
		this.env = env;
	}

	@Override
	@Observed(name = "function_profile")
	protected String getAssetPath() {
		return config.getDocumentPath();
	}

	@Override
	@Observed(name = "function_profile")
	protected String getAssetIndex() {
		return elasticConfig.getDocumentIndex();
	}

	@Override
	public String getAssetAlias() {
		return elasticConfig.getDocumentAlias();
	}

	@Override
	@Observed(name = "function_profile")
	public DocumentAsset createAsset(
		final DocumentAsset asset,
		final UUID projectId,
		final Schema.Permission hasWritePermission
	) throws IOException {
		return super.createAsset(asset, projectId, hasWritePermission);
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

	@Override
	@Observed(name = "function_profile")
	public Optional<DocumentAsset> updateAsset(
		final DocumentAsset asset,
		final UUID projectId,
		final Schema.Permission hasWritePermission
	) throws IOException, IllegalArgumentException {
		final Optional<DocumentAsset> originalOptional = getAsset(asset.getId(), hasWritePermission);
		if (originalOptional.isEmpty()) {
			return Optional.empty();
		}

		final DocumentAsset original = originalOptional.get();

		// Preserve ownership. This may be coming from KM which doesn't have an
		// awareness of who owned this document.
		asset.setUserId(original.getUserId());

		final Optional<DocumentAsset> updatedOptional = super.updateAsset(asset, projectId, hasWritePermission);
		if (updatedOptional.isEmpty()) {
			return Optional.empty();
		}

		final DocumentAsset updated = updatedOptional.get();

		if (!isRunningTestProfile() && updated.getPublicAsset() && !updated.getTemporary()) {
			if (updated.getMetadata() != null && updated.getMetadata().containsKey("gollmCard")) {
				// update embeddings
				final JsonNode card = updated.getMetadata().get("gollmCard");
				final String cardText = objectMapper.writeValueAsString(card);

				new Thread(() -> {
					try {
						final TerariumAssetEmbeddings embeddings = embeddingService.generateEmbeddings(cardText);

						// Execute the update request
						uploadEmbeddings(updated.getId(), embeddings, hasWritePermission);
					} catch (final Exception e) {
						log.error("Failed to update embeddings for document {}", updated.getId(), e);
					}
				}).start();
			}
		}
		return updatedOptional;
	}
}
