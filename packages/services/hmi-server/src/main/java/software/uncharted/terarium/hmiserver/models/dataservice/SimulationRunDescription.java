package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;
import java.time.Instant;

@Data
@Accessors(chain = true)
public class SimulationRunDescription implements Serializable {

  private String id;

  @JsonbProperty("simulator_id")
  private String simulatorId;

  @JsonbProperty("timestamp")
  private Instant startTimestamp;

  @JsonbProperty("completed_at")
  private Instant endTimestamp;

  private Boolean success;

  private String response;
}
