package software.uncharted.terarium.hmiserver.models.climateData;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class ClimateDataResponse {
    private String id;
    private String queued;
    private JsonNode result;
}
