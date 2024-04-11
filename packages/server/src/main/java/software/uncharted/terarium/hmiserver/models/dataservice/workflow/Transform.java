package software.uncharted.terarium.hmiserver.models.dataservice.workflow;

import java.io.Serializable;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
public class Transform implements Serializable {

  Number x;
  Number y;
  Number k;
}
