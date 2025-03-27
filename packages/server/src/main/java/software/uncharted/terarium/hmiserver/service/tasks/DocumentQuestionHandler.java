package software.uncharted.terarium.hmiserver.service.tasks;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentQuestionHandler extends LlmTaskResponseHandler {

	public static final String NAME = "gollm:document_question";

	@Override
	public String getName() {
		return NAME;
	}

	@Data
	public static class Input extends LlmTaskResponseHandler.Input {

		@JsonProperty("document")
		String document;

		@JsonProperty("question")
		String question;
	}

	@Data
	public static class Properties {

		private UUID projectId;
		private UUID documentId;
	}

	@Data
	private static class ResponseOutput {

		private String response;
	}
}
