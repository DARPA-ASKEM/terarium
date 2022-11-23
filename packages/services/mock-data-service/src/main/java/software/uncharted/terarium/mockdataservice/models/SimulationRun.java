package software.uncharted.terarium.mockdataservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.json.bind.annotation.JsonbProperty;
import java.time.Instant;

@Data
@AllArgsConstructor
public class SimulationRun {

	@JsonbProperty("id")
	private Long id;

	@JsonbProperty("simulator_id")
	private Long modelId;

	@JsonbProperty("timestamp")
	private Instant startTimestamp = Instant.now().minusSeconds(3600L);

	@JsonbProperty("completed_at")
	private Instant endTimestamp = Instant.now();

	@JsonbProperty("success")
	private Boolean success;

	@JsonbProperty("response")
	private String response;

	public SimulationRun(final Long id, final Long modelId, final Boolean success, final String response) {
		this.id = id;
		this.modelId = modelId;
		this.success = success;
		this.response = response;
	}
}
