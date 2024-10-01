package software.uncharted.terarium.hmiserver.service.tasks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class EnrichAmrResponseHandler extends TaskResponseHandler {

	public static final String NAME = "gollm:enrich_amr";

	private final ObjectMapper objectMapper;
	private final ModelService modelService;

	@Override
	public String getName() {
		return NAME;
	}

	@Data
	public static class Input {

		@JsonProperty("research_paper")
		String researchPaper;

		@JsonProperty("amr")
		String amr;
	}

	@Data
	public static class Response {

		ModelMetadata response;
	}

	@Data
	public static class Properties {

		UUID projectId;
		UUID documentId;
		UUID modelId;
		Boolean overwrite;
	}

	@Override
	public TaskResponse onSuccess(final TaskResponse resp) {
		try {
			final Properties props = resp.getAdditionalProperties(Properties.class);

			final Response response = objectMapper.readValue(resp.getOutput(), Response.class);

			final Model model = modelService
				.getAsset(props.getModelId(), ASSUME_WRITE_PERMISSION_ON_BEHALF_OF_USER)
				.orElseThrow();

			model.setMetadata(response.getResponse());
		} catch (final Exception e) {
			log.error("Failed to enrich amr", e);
			throw new RuntimeException(e);
		}
		log.info("Model enriched successfully");
		return resp;
	}
}
