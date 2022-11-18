package software.uncharted.terarium.hmiserver.models;

import javax.json.bind.annotation.JsonbProperty;
import java.time.Instant;


public class SimulationRun {

	@JsonbProperty("id")
	public Long id;

	@JsonbProperty("simulator_id")
	public Long modelId;

	@JsonbProperty("timestamp")
	public Instant startTimestamp;

	@JsonbProperty("completed_at")
	public Instant endTimestamp;

	@JsonbProperty("success")
	public Boolean success;

	@JsonbProperty("response")
	public String response;
}
