package software.uncharted.terarium.hmiserver.models.funman.models;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
public class FunmanBoxTypes {
    private String type;
    private String label;
    private FunmanBounds bounds;
}
