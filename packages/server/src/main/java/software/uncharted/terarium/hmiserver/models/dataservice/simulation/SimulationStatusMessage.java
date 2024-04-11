package software.uncharted.terarium.hmiserver.models.dataservice.simulation;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class SimulationStatusMessage {

  private String status;

  @JsonAlias("error_msg")
  private String errorMsg;
}
