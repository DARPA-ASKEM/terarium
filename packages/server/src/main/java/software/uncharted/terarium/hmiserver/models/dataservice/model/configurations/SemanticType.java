package software.uncharted.terarium.hmiserver.models.dataservice.model.configurations;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SemanticType {
	@JsonProperty("initial")
	INITIAL,

	@JsonProperty("parameter")
	PARAMETER,

	@JsonProperty("observable")
	OBSERVABLE,

	@JsonProperty("inferredParameter")
	INFERRED
}
