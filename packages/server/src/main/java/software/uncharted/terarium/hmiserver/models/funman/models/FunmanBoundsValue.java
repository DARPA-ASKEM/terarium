package software.uncharted.terarium.hmiserver.models.funman.models;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
public class FunmanBoundsValue {
    private Double lb;
    private Double ub;
}
