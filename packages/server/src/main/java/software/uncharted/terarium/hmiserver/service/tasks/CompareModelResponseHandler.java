package software.uncharted.terarium.hmiserver.service.tasks;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CompareModelResponseHandler extends TaskResponseHandler {
	final public static String NAME = "gollm:compare_model";

	public String getName() {
		return NAME;
	}

	@Data
	public static class Input {
		@JsonProperty("model_cards")
		List<JsonNode> modelCards;
	}

	@Data
	public static class Properties {
		List<UUID> modelIds;
	}

	@Data
	public static class Response {
		JsonNode response;
	}

}
