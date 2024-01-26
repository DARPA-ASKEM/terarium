package software.uncharted.terarium.hmiserver.models;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import software.uncharted.terarium.hmiserver.annotations.TSIgnore;

public interface SupportAdditionalProperties {

	@TSIgnore
	public Map<String, Object> additionalProperties = new HashMap<>();

	@JsonAnyGetter
	@TSIgnore
	default public Map<String, Object> getAdditionalProperties() {
		return additionalProperties;
	}

	@JsonAnySetter
	@TSIgnore
	default public void setAdditionalProperties(String name, Object value) {
		additionalProperties.put(name, value);
	}
}
