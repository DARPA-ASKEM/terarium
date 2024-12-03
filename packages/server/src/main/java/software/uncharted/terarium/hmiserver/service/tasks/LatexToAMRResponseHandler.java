package software.uncharted.terarium.hmiserver.service.tasks;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class LatexToAMRResponseHandler extends TaskResponseHandler {

	public static final String NAME = "mira_task:latex_to_amr";

	@Override
	public String getName() {
		return NAME;
	}

	@Data
	public static class Response {

		String response;
	}

	@Override
	public TaskResponse onSuccess(final TaskResponse resp) {
		return resp;
	}
}
