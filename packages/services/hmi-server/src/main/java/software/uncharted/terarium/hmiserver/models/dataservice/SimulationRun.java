package software.uncharted.terarium.hmiserver.models.dataservice;

import javax.json.bind.annotation.JsonbProperty;
import java.time.Instant;


public class SimulationRun {

	@JsonbProperty("id")
	public String id = null;

	@JsonbProperty("simulator_id")
	public String simulatorId;

	@JsonbProperty("timestamp")
	public Instant startTimestamp;

	@JsonbProperty("completed_at")
	public Instant endTimestamp;

	@JsonbProperty("success")
	public Boolean success;

	@JsonbProperty("response")
	public String response;

	public SimulationRun(final String simulatorId, final Instant startTimestamp, final Instant endTimestamp, final Boolean success, final String response) {
		this.simulatorId = simulatorId;
		this.startTimestamp = startTimestamp;
		this.endTimestamp = endTimestamp;
		this.success = success;
		this.response = response;
	}
}
