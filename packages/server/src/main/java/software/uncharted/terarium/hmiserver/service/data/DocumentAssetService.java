package software.uncharted.terarium.hmiserver.service.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.repository.data.DocumentRepository;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;

@Slf4j
@Service
public class DocumentAssetService extends TerariumAssetService<DocumentAsset, DocumentRepository> {

	public DocumentAssetService(
		final ObjectMapper objectMapper,
		final Config config,
		final ProjectService projectService,
		final ProjectAssetService projectAssetService,
		final DocumentRepository repository,
		final S3ClientService s3ClientService
	) {
		super(objectMapper, config, projectService, projectAssetService, repository, s3ClientService, DocumentAsset.class);
	}

	@Override
	@Observed(name = "function_profile")
	protected String getAssetPath() {
		return config.getDocumentPath();
	}

	@Override
	@Observed(name = "function_profile")
	public DocumentAsset createAsset(final DocumentAsset asset, final UUID projectId) throws IOException {
		return super.createAsset(asset, projectId);
	}

	@Override
	@Observed(name = "function_profile")
	public Optional<DocumentAsset> updateAsset(final DocumentAsset asset, final UUID projectId)
		throws IOException, IllegalArgumentException {
		final Optional<DocumentAsset> originalOptional = getAsset(asset.getId());
		if (originalOptional.isEmpty()) {
			return Optional.empty();
		}

		final DocumentAsset original = originalOptional.get();

		// Preserve ownership. This may be coming from KM which doesn't have an
		// awareness of who owned this document.
		asset.setUserId(original.getUserId());

		return super.updateAsset(asset, projectId);
	}
}
