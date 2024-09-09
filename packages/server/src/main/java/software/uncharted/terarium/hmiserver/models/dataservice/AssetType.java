package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.code.Code;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.model.configurations.ModelConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.Simulation;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.Workflow;
import software.uncharted.terarium.hmiserver.models.simulationservice.interventions.InterventionPolicy;

@RequiredArgsConstructor
@TSModel
@Slf4j
public enum AssetType {
	@JsonProperty("workflow")
	WORKFLOW,

	@JsonProperty("model")
	MODEL,

	@JsonProperty("dataset")
	DATASET,

	@JsonProperty("simulation")
	SIMULATION,

	@JsonProperty("document")
	DOCUMENT,

	@JsonProperty("code")
	CODE,

	@JsonProperty("model-configuration")
	MODEL_CONFIGURATION,

	@JsonProperty("artifact")
	ARTIFACT,

	@JsonProperty("intervention-policy")
	INTERVENTION_POLICY;

	public static AssetType getAssetType(final String assetTypeName, final ObjectMapper objectMapper)
		throws ResponseStatusException {
		try {
			return objectMapper.convertValue(assetTypeName, AssetType.class);
		} catch (final IllegalArgumentException iae) {
			log.error("Error converting the string assetTypeName into a valid AssetType", iae);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to convert an AssetTypeName into an AssetType");
		}
	}

	public Class<? extends TerariumAsset> getAssetClass() {
		switch (this) {
			case ARTIFACT:
				return Artifact.class;
			case CODE:
				return Code.class;
			case DATASET:
				return Dataset.class;
			case DOCUMENT:
				return DocumentAsset.class;
			case MODEL:
				return Model.class;
			case MODEL_CONFIGURATION:
				return ModelConfiguration.class;
			case SIMULATION:
				return Simulation.class;
			case WORKFLOW:
				return Workflow.class;
			case INTERVENTION_POLICY:
				return InterventionPolicy.class;
			default:
				throw new IllegalArgumentException("Unrecognized asset type: " + this);
		}
	}
}
