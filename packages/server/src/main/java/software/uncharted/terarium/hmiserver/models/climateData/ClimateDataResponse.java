package software.uncharted.terarium.hmiserver.models.climateData;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class ClimateDataResponse {

	private String id;
	private String queued;
	private Result result;

	@Data
	public static class Result {

		@JsonProperty("created_at")
		private String createdAt;

		@JsonProperty("enqueued_at")
		private String enqueuedAt;

		@JsonProperty("started_at")
		private String startedAt;

		@JsonProperty("job_result")
		private JsonNode jobResult;

		@JsonProperty("job_error")
		private JsonNode jobError;
	}
}
