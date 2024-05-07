package software.uncharted.terarium.hmiserver.models;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.UUID;

@Data
public class JsonNodeProjectIdRequestBody {
    JsonNode jsonNode;
    UUID projectId;
}
