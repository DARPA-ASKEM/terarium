package software.uncharted.terarium.hmiserver.service.data;

import io.micrometer.observation.annotation.Observed;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.Artifact;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.code.Code;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.model.configurations.ModelConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.notebooksession.NotebookSession;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.Simulation;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.Workflow;
import software.uncharted.terarium.hmiserver.models.simulationservice.interventions.InterventionPolicy;
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
	private final InterventionService interventionService;
	private final SimulationService simulationService;
	private final NotebookSessionService notebookSessionService;

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
			case INTERVENTION_POLICY -> interventionService;
			case SIMULATION -> simulationService;
			case NOTEBOOK_SESSION -> notebookSessionService;
			default -> throw new IllegalArgumentException("Invalid asset type: " + type);
		};
	}

	public Optional<? extends TerariumAsset> updateAsset(
		final TerariumAsset asset,
		final UUID projectId,
		final AssetType type,
		final Permission permission
	) throws IOException {
		switch (type) {
			case ARTIFACT:
				return artifactService.updateAsset((Artifact) asset, projectId, permission);
			case CODE:
				return codeService.updateAsset((Code) asset, projectId, permission);
			case DATASET:
				return datasetService.updateAsset((Dataset) asset, projectId, permission);
			case DOCUMENT:
				return documentAssetService.updateAsset((DocumentAsset) asset, projectId, permission);
			case MODEL_CONFIGURATION:
				return modelConfigurationService.updateAsset((ModelConfiguration) asset, projectId, permission);
			case MODEL:
				return modelService.updateAsset((Model) asset, projectId, permission);
			case WORKFLOW:
				return workflowService.updateAsset((Workflow) asset, projectId, permission);
			case INTERVENTION_POLICY:
				return interventionService.updateAsset((InterventionPolicy) asset, projectId, permission);
			case SIMULATION:
				return simulationService.updateAsset((Simulation) asset, projectId, permission);
			case NOTEBOOK_SESSION:
				return notebookSessionService.updateAsset((NotebookSession) asset, projectId, permission);
			default:
				throw new IllegalArgumentException("Invalid asset type: " + type);
		}
	}
}
