package software.uncharted.terarium.hmiserver.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class AMRToMMTResponseHandler extends TaskResponseHandler {
	public static final String NAME = "mira_task:amr_to_mmt";

	private final ObjectMapper objectMapper;

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
		try {
			final Response modelResp = objectMapper.readValue(resp.getOutput(), Response.class);
			resp.setOutput(modelResp.getResponse().getBytes());
		} catch (final Exception e) {
			log.error("Failed to create model", e);
			throw new RuntimeException(e);
		}
		return resp;
	}
}
