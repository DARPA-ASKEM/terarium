package software.uncharted.terarium.hmiserver.models.simulationservice.interventions;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum InterventionValueType {
	@JsonProperty("value")
	VALUE,

	@JsonProperty("percentage")
	PERCENTAGE
}
