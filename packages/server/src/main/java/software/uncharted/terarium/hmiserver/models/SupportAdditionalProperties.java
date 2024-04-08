package software.uncharted.terarium.hmiserver.models;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import software.uncharted.terarium.hmiserver.annotations.TSIgnore;

public class SupportAdditionalProperties {

	@TSIgnore
	protected Map<String, Object> additionalProperties = new HashMap<>();

	@JsonAnyGetter
	@TSIgnore
	public Map<String, Object> getAdditionalProperties() {
		return additionalProperties;
	}

	@JsonAnySetter
	@TSIgnore
	public void setAdditionalProperty(final String name, final Object value) {
		additionalProperties.put(name, value);
	}

	@TSIgnore
	public void setAdditionalProperties(final Map<String, Object> props) {
		additionalProperties = props;
	}
}
