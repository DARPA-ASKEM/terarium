package software.uncharted.terarium.hmiserver.service.tasks;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompareModelsResponseHandler extends TaskResponseHandler {

	public static final String NAME = "gollm:compare_models";

	@Override
	public String getName() {
		return NAME;
	}

	@Data
	public static class Input {

		List<String> amrs;
		String goal;
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
