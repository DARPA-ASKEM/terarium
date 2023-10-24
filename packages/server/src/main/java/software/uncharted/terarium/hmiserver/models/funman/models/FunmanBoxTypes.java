package software.uncharted.terarium.hmiserver.models.funman.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@TSModel
public class FunmanBoxTypes {
    private String type;
    private String label;
    private FunmanBounds bounds;
}
