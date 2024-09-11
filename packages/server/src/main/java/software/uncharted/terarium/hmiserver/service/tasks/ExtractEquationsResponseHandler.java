package software.uncharted.terarium.hmiserver.service.tasks;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExtractEquationsResponseHandler extends TaskResponseHandler {

	public static final String NAME = "nougat_task:extraction";

	@Override
	public String getName() {
		return NAME;
	}

	@Data
	public static class Input {

		byte[] pdf;
	}

	@Data
	public static class ResponseOutput {

		private String response;
	}
}
