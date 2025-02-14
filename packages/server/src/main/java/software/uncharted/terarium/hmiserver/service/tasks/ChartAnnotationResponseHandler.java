package software.uncharted.terarium.hmiserver.service.tasks;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.uncharted.terarium.hmiserver.models.dataservice.ChartAnnotation.ChartAnnotationType;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChartAnnotationResponseHandler extends TaskResponseHandler {

	public static final String NAME = "gollm:chart_annotation";

	@Override
	public String getName() {
		return NAME;
	}

	@Data
	public static class Input {

		String preamble;
		String instruction;
		ChartAnnotationType chartType;
	}

	@Data
	private static class ResponseOutput {

		private String response;
	}
}
