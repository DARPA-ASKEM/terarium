package software.uncharted.terarium.hmiserver.service.tasks;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
// @Slf4j
public class GenerateSummaryHandler extends TaskResponseHandler {
	public static final String NAME = "gollm_task:generate_summary";

	@Override
	public String getName() {
		return NAME;
	}
}
