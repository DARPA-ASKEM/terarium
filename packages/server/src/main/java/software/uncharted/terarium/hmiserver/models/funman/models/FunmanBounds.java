package software.uncharted.terarium.hmiserver.models.funman.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import java.util.Map;

@Data
@Accessors(chain = true)
@TSModel
public class FunmanBounds {
    @JsonProperty("step_size")
    private FunmanBoundsValue stepSize;

    @JsonProperty("num_steps")
    private FunmanBoundsValue numSteps;

    private Map<String, FunmanBoundsValue> additionalBounds;
}
