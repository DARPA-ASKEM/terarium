package software.uncharted.terarium.hmiserver.service.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SympyToAMRResponseHandler extends TaskResponseHandler {

	public static final String NAME = "mira_task:sympy_to_amr";

	@Override
	public String getName() {
		return NAME;
	}
}
