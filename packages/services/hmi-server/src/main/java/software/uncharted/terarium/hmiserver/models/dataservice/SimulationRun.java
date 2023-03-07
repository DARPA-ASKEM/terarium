package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

@Data
@Accessors(chain = true)
public class SimulationRun implements Serializable {

	private Long id;

	@JsonAlias("simulator_id")
	private String simulatorId;

	@JsonAlias("timestamp")
	private Instant startTimestamp;

	@JsonAlias("completed_at")
	private Instant endTimestamp;

	private Boolean success;

	private String response;

	private Map<String, String> parameters;
}
