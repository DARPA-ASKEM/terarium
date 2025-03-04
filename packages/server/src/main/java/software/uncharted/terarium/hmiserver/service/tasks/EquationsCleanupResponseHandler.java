package software.uncharted.terarium.hmiserver.service.tasks;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EquationsCleanupResponseHandler extends LlmTaskResponseHandler {

	public static final String NAME = "gollm:equations_cleanup";

	@Override
	public String getName() {
		return NAME;
	}

	@Data
	public static class Input extends LlmTaskResponseHandler.Input {

		List<String> equations;
	}

	@Data
	public static class Properties {

		UUID projectId;
	}

	@Data
	public static class Response {

		JsonNode response;
	}
}
