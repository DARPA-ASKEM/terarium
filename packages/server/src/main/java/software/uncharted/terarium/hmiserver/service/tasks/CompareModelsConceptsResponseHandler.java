package software.uncharted.terarium.hmiserver.service.tasks;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompareModelsConceptsResponseHandler extends TaskResponseHandler {

	public static final String NAME = "mira_task:model_comparison";

	@Override
	public String getName() {
		return NAME;
	}

	@Data
	public static class Input {

		List<String> amrs;
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
