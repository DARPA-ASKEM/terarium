package software.uncharted.terarium.hmiserver.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.HashMap;
import java.util.Map;
import software.uncharted.terarium.hmiserver.annotations.TSIgnore;

public class TerariumAssetThatSupportsAdditionalProperties extends TerariumAsset {

    @TSIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonAnyGetter
    @TSIgnore
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    @JsonAnySetter
    @TSIgnore
    public void setAdditionalProperties(final String name, final Object value) {
        additionalProperties.put(name, value);
    }
}
