package software.uncharted.terarium.hmiserver.service.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AMRToMMTResponseHandler extends TaskResponseHandler {

	public static final String NAME = "mira_task:amr_to_mmt";

	@Override
	public String getName() {
		return NAME;
	}
}
