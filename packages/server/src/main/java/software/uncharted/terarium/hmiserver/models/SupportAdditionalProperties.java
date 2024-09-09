package software.uncharted.terarium.hmiserver.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import software.uncharted.terarium.hmiserver.annotations.TSIgnore;

/**
 * This class integrates with Jackson to support additional untyped properties. Any property that is not explicitly
 * typed on the class will be stored behind the scenes in a map. This preserves them through serialization /
 * deserialization. This is useful for semi-blackbox types where we have certain concrete fields that we know of, and
 * other dynamic fields we don't.
 */
public class SupportAdditionalProperties implements Cloneable {

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

	@TSIgnore
	public void setAdditionalProperties(final Map<String, JsonNode> props) {
		additionalProperties = props;
	}

	@Override
	public SupportAdditionalProperties clone() {
		final SupportAdditionalProperties clone;
		try {
			clone = (SupportAdditionalProperties) super.clone();
		} catch (final CloneNotSupportedException e) {
			// this won't be hit.
			throw new RuntimeException(e);
		}
		if (this.additionalProperties != null) {
			clone.additionalProperties = new HashMap<>();
			for (final String key : additionalProperties.keySet()) {
				clone.getAdditionalProperties().put(key, additionalProperties.get(key).deepCopy());
			}
		}

		return clone;
	}
}
