package software.uncharted.terarium.hmiserver.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.HashMap;
import java.util.Map;
import software.uncharted.terarium.hmiserver.annotations.TSIgnore;

/**
 * This class integrates with Jackson to support additional untyped properties. Any property that is not explicitly
 * typed on the class will be stored behind the scenes in a map. This preserves them through serialization /
 * deserialization. This is useful for semi-blackbox types where we have certain concrete fields that we know of, and
 * other dynamic fields we don't.
 */
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
