package software.uncharted.terarium.hmiserver.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.service.data.ModelService;

@Component
@RequiredArgsConstructor
@Slf4j
public class SbmlToPetrinetResponseHandler extends TaskResponseHandler {
	final static public String NAME = "mira_task:sbml_to_petrinet";

	final private ObjectMapper objectMapper;
	final private ModelService modelService;

	@Override
	public String getName() {
		return NAME;
	}

	@Data
	public static class Response {
		Model response;
	}

	@Override
	public TaskResponse onSuccess(final TaskResponse resp) {
		try {
			final Response modelResp = objectMapper.readValue(resp.getOutput(), Response.class);
			Model model = modelResp.getResponse();

			model.getSemantics().getOde().getParameters().forEach((param) -> {
				if (param.getName() == null || param.getName().isEmpty()) {
					param.setName(param.getId());
				}
			});

			model = modelService.createAsset(modelResp.getResponse());
			resp.setOutput(objectMapper.writeValueAsString(model).getBytes());
		} catch (final Exception e) {
			log.error("Failed to create model", e);
			throw new RuntimeException(e);
		}
		return resp;
	}
}
