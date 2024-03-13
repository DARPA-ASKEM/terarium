package software.uncharted.terarium.hmiserver.models.climateData;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class ClimateDataResponse {
    private String id;
    private String queued;
    private Result result;

    @Data
    public static class Result {
        private String created_at;
        private String enqueued_at;
        private String started_at;
        private JsonNode job_result;
        private JsonNode job_error;
    }
}
