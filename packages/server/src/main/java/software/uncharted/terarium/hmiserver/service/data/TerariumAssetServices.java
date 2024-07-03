package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.util.Optional;

import org.springframework.stereotype.Service;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.Artifact;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.code.Code;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.model.configurations.ModelConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.Workflow;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema.Permission;

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

	public Optional<? extends TerariumAsset> updateAsset(final TerariumAsset asset, final AssetType type,
			final Permission permission) throws IOException {

		switch (type) {
			case ARTIFACT:
				return artifactService.updateAsset((Artifact) asset, permission);
			case CODE:
				return codeService.updateAsset((Code) asset, permission);
			case DATASET:
				return datasetService.updateAsset((Dataset) asset, permission);
			case DOCUMENT:
				return documentAssetService.updateAsset((DocumentAsset) asset, permission);
			case MODEL_CONFIGURATION:
				return modelConfigurationService.updateAsset((ModelConfiguration) asset,
						permission);
			case MODEL:
				return modelService.updateAsset((Model) asset, permission);
			case WORKFLOW:
				return workflowService.updateAsset((Workflow) asset, permission);
			default:
				throw new IllegalArgumentException("Invalid asset type: " + type);
		}
	}
}
