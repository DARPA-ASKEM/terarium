package software.uncharted.terarium.hmiserver.service.tasks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.service.data.DocumentAssetService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ModelCardResponseHandler extends TaskResponseHandler {

	public static final String NAME = "gollm_task:model_card";
	private final ObjectMapper objectMapper;
	private final DocumentAssetService documentAssetService;

	public static final int MAX_TEXT_SIZE = 600000;

	@Override
	public String getName() {
		return NAME;
	}

	@Data
	public static class Input {

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
	}

	@Override
	public TaskResponse onSuccess(final TaskResponse resp) {
		try {
			final Properties props = resp.getAdditionalProperties(Properties.class);
			if (props == null) {
				// just return the response
				return resp;
			}

			log.info("Writing model card to database for document {}", props.getDocumentId());
			final DocumentAsset document = documentAssetService
				.getAsset(props.getDocumentId(), ASSUME_WRITE_PERMISSION_ON_BEHALF_OF_USER)
				.orElseThrow();
			final Response card = objectMapper.readValue(resp.getOutput(), Response.class);
			if (document.getMetadata() == null) {
				document.setMetadata(new java.util.HashMap<>());
			}
			document.getMetadata().put("gollmCard", card.response);
			documentAssetService.updateAsset(document, props.projectId, ASSUME_WRITE_PERMISSION_ON_BEHALF_OF_USER);
		} catch (final Exception e) {
			log.error("Failed to write model card to database", e);
			throw new RuntimeException(e);
		}
		return resp;
	}
}
