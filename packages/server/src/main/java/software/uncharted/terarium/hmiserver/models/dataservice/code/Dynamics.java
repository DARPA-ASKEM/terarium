package software.uncharted.terarium.hmiserver.models.dataservice.code;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
public class Dynamics {
  private String name;
  private String description;
  private List<String> block;
}
