package software.uncharted.terarium.hmiserver.service.tasks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompareModelsResponseHandler extends TaskResponseHandler {
	public static final String NAME = "gollm_task:compare_models";

	@Override
	public String getName() {
		return NAME;
	}

	@Data
	public static class Input {
		@JsonProperty("model_cards")
		List<String> modelCards;
	}

	@Data
	public static class Properties {
		List<UUID> modelIds;
		UUID workflowId;
		UUID nodeId;
	}

	@Data
	public static class Response {
		JsonNode response;
	}
}
