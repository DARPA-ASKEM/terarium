package software.uncharted.terarium.hmiserver.models.dataservice.code;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
@TSModel
public class Dynamics {
    private String name;
    private String description;
    private List<String> block;
}