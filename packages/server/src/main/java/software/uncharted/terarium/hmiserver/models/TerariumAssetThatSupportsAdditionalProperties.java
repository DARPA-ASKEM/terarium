package software.uncharted.terarium.hmiserver.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import software.uncharted.terarium.hmiserver.annotations.TSIgnore;

public class TerariumAssetThatSupportsAdditionalProperties extends TerariumAsset {

	@TSIgnore
	protected Map<String, JsonNode> additionalProperties = new HashMap<>();

	@JsonAnyGetter
	@TSIgnore
	public Map<String, JsonNode> getAdditionalProperties() {
		return additionalProperties;
	}

	@JsonAnySetter
	@TSIgnore
	public void setAdditionalProperty(final String name, final Object value) {
		final ObjectMapper mapper = new ObjectMapper();
		additionalProperties.put(name, mapper.valueToTree(value));
	}
}
