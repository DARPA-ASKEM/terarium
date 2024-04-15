package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@RequiredArgsConstructor
@TSModel
@Slf4j
public enum AssetType {
    @JsonProperty("workflow")
    WORKFLOW,

    @JsonProperty("model")
    MODEL,

    @JsonProperty("dataset")
    DATASET,

    @JsonProperty("simulation")
    SIMULATION,

    @JsonProperty("document")
    DOCUMENT,

    @JsonProperty("code")
    CODE,

    @JsonProperty("model-configuration")
    MODEL_CONFIGURATION,

    @JsonProperty("artifact")
    ARTIFACT,

    @JsonProperty("publication")
    PUBLICATION,

    @JsonProperty("notebook-session")
    NOTEBOOK_SESSION,
    ;

    public static AssetType getAssetType(final String assetTypeName, final ObjectMapper objectMapper)
            throws ResponseStatusException {
        try {
            return objectMapper.convertValue(assetTypeName, AssetType.class);
        } catch (final IllegalArgumentException iae) {
            log.error("Error converting the string assetTypeName into a valid AssetType", iae);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Failed to convert an AssetTypeName into an AssetType");
        }
    }
}
