package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class SimulationRun implements Serializable {

	private Long id;

	@JsonProperty("simulator_id")
	private String simulatorId;

	@JsonProperty("timestamp")
	private Instant startTimestamp;

	@JsonProperty("completed_at")
	private Instant endTimestamp;

	private Boolean success;

	private String response;

	private List<Object> parameters;
}
