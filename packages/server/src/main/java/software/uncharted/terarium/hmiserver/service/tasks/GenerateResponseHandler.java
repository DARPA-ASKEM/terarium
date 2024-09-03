package software.uncharted.terarium.hmiserver.service.tasks;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenerateResponseHandler extends TaskResponseHandler {

	public static final String NAME = "gollm_task:generate_response";

	@Override
	public String getName() {
		return NAME;
	}

	@Data
	public static class Input {

		String instruction;
		JsonNode responseFormat;
	}

	@Data
	private static class ResponseOutput {

		private String response;
	}
}
