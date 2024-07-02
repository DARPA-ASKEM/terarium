package software.uncharted.terarium.hmiserver.service.data;

import org.springframework.stereotype.Service;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;

@Service
@RequiredArgsConstructor
public class TerariumAssetServices {

	private final ArtifactService artifactService;
	private final CodeService codeService;
	private final DatasetService datasetService;
	private final DocumentAssetService documentAssetService;
	private final ModelConfigurationService modelConfigurationService;
	private final ModelService modelService;
	private final WorkflowService workflowService;

	/**
	 * Get the service for a given asset type
	 *
	 * @param type The type of asset to get the service for
	 * @return The service for the given asset type
	 */
	@Observed(name = "function_profile")
	public ITerariumAssetService<? extends TerariumAsset> getServiceByType(final AssetType type) {
		return switch (type) {
			case ARTIFACT -> artifactService;
			case CODE -> codeService;
			case DATASET -> datasetService;
			case DOCUMENT -> documentAssetService;
			case MODEL_CONFIGURATION -> modelConfigurationService;
			case MODEL -> modelService;
			case WORKFLOW -> workflowService;
			default -> throw new IllegalArgumentException("Invalid asset type: " + type);
		};
	}
}
