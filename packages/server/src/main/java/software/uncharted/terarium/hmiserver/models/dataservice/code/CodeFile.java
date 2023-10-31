package software.uncharted.terarium.hmiserver.models.dataservice.code;

import software.uncharted.terarium.hmiserver.models.dataservice.code.Dynamics;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
public class CodeFile {
    private String language;
    private Dynamics dynamics;
}