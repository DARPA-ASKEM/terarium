package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Instant;

@Data
@Accessors(chain = true)
public class SimulationRunDescription implements Serializable {

	private String id;

	@JsonProperty("simulator_id")
	private String simulatorId;

	@JsonProperty("timestamp")
	private Instant startTimestamp;

	@JsonProperty("completed_at")
	private Instant endTimestamp;

	private Boolean success;

	private String response;
}
