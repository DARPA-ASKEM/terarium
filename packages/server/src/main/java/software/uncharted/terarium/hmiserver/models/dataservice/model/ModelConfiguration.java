package software.uncharted.terarium.hmiserver.models.dataservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serial;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.TerariumAssetThatSupportsAdditionalProperties;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TSModel
public class ModelConfiguration extends TerariumAssetThatSupportsAdditionalProperties {

    @Serial
    private static final long serialVersionUID = -4109896135386019667L;

    private String name;

    @TSOptional
    private String description;

    @JsonProperty("model_id")
    private UUID modelId;

    private Object configuration;
}
