package software.uncharted.terarium.hmiserver.models.funman.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@TSModel
public class FunmanBoundsValue {
    private Double lb;
    private Double ub;
}
