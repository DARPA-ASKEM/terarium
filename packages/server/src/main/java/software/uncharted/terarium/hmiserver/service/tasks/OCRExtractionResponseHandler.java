package software.uncharted.terarium.hmiserver.service.tasks;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OCRExtractionResponseHandler extends TaskResponseHandler {

	public static final String NAME = "ocr_extraction_task:ocr_extraction";

	@Override
	public String getName() {
		return NAME;
	}

	@Data
	public static class ResponseOutput {

		private JsonNode response;
	}
}
