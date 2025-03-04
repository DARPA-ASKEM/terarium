package software.uncharted.terarium.hmiserver.service.tasks;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

public abstract class LlmTaskResponseHandler extends TaskResponseHandler {

	@Data
	public static class Input {

		@JsonProperty("llm")
		String llm;
	}
}
