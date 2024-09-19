package software.uncharted.terarium.hmiserver.service.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenerateModelLatexResponseHandler extends TaskResponseHandler {

	public static final String NAME = "mira_task:generate_model_latex";

	@Override
	public String getName() {
		return NAME;
	}
}
