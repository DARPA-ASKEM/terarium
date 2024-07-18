package software.uncharted.terarium.hmiserver.models.simulationservice.interventions;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum InterventionSemanticType {
	@JsonProperty("variable")
	VARIABLE,

	@JsonProperty("parameter")
	PARAMETER
}
