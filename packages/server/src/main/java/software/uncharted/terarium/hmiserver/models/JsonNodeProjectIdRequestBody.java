package software.uncharted.terarium.hmiserver.models;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;
import lombok.Data;

@Data
public class JsonNodeProjectIdRequestBody {
	JsonNode jsonNode;
	UUID projectId;
}
