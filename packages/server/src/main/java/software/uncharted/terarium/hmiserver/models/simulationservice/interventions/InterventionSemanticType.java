package software.uncharted.terarium.hmiserver.models.simulationservice.interventions;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum InterventionSemanticType {
	@JsonProperty("state")
	STATE,

	@JsonProperty("parameter")
	PARAMETER
}
