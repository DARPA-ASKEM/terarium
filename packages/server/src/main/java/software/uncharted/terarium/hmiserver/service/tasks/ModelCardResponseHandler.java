package software.uncharted.terarium.hmiserver.service.tasks;

import static software.uncharted.terarium.hmiserver.utils.JsonToHTML.renderJsonToHTML;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelMetadata;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.service.data.ModelService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ModelCardResponseHandler extends TaskResponseHandler {

	public static final String NAME = "gollm:model_card";
	private final ObjectMapper objectMapper;

	private final ModelService modelService;

	public static final int MAX_TEXT_SIZE = 600000;

	@Override
	public String getName() {
		return NAME;
	}

	@Data
	public static class Input {

		@JsonProperty("amr")
		String amr;

		@JsonProperty("research_paper")
		String researchPaper;
	}

	@Data
	public static class Response {

		JsonNode response;
	}

	@Data
	public static class Properties {

		UUID projectId;
		UUID documentId;
		UUID modelId;
	}

	@Override
	public TaskResponse onSuccess(final TaskResponse resp) {
		try {
			final Properties props = resp.getAdditionalProperties(Properties.class);
			if (props == null) {
				// just return the response
				return resp;
			}

			log.info("Writing model card to database for model {}", props.getModelId());
			// Grab the model
			final Model model = modelService.getAsset(props.modelId, ASSUME_WRITE_PERMISSION_ON_BEHALF_OF_USER).orElseThrow();

			final Response card = objectMapper.readValue(resp.getOutput(), Response.class);
			if (model.getMetadata() == null) {
				model.setMetadata(new ModelMetadata());
			}
			model.getMetadata().setGollmCard(card.response);
			model.getMetadata().setDescription(renderJsonToHTML(card.response).getBytes(StandardCharsets.UTF_8));
			modelService.updateAsset(model, props.modelId, ASSUME_WRITE_PERMISSION_ON_BEHALF_OF_USER);
		} catch (final Exception e) {
			log.error("Failed to write model card to database", e);
			throw new RuntimeException(e);
		}
		return resp;
	}
}
