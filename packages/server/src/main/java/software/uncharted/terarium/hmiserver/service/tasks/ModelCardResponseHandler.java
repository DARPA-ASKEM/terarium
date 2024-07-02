package software.uncharted.terarium.hmiserver.service.tasks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.uncharted.terarium.hmiserver.models.TerariumAssetEmbeddings;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.service.data.DocumentAssetService;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
import software.uncharted.terarium.hmiserver.service.gollm.EmbeddingService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ModelCardResponseHandler extends TaskResponseHandler {
	public static final String NAME = "gollm_task:model_card";
	private final ObjectMapper objectMapper;
	private final DocumentAssetService documentAssetService;
	private final ModelService modelService;
	private final EmbeddingService embeddingService;

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
		UUID documentId;
		boolean updateEmbeddings = false;
	}

	@Override
	public TaskResponse onSuccess(final TaskResponse resp) {
		try {
			final Properties props = resp.getAdditionalProperties(Properties.class);
			log.info("Writing model card to database for document {}", props.getDocumentId());

			final DocumentAsset document = documentAssetService
					.getAsset(props.getDocumentId(), ASSUME_WRITE_PERMISSION_ON_BEHALF_OF_USER)
					.orElseThrow();
			final Response card = objectMapper.readValue(resp.getOutput(), Response.class);
			if (document.getMetadata() == null) {
				document.setMetadata(new java.util.HashMap<>());
			}
			document.getMetadata().put("gollmCard", card.response);
			documentAssetService.updateAsset(document, ASSUME_WRITE_PERMISSION_ON_BEHALF_OF_USER);

			// if marked to update embeddings, do so
			if (props.updateEmbeddings && document.getPublicAsset() && !document.getTemporary()) {
				final String cardText = objectMapper.writeValueAsString(card.response);
				try {
					final TerariumAssetEmbeddings embeddings = embeddingService.generateEmbeddings(cardText);

					documentAssetService.uploadEmbeddings(
							document.getId(), embeddings, ASSUME_WRITE_PERMISSION_ON_BEHALF_OF_USER);

				} catch (final Exception e) {
					log.error("Failed to update embeddings for document {}", document.getId(), e);
				}
			}

		} catch (final Exception e) {
			log.error("Failed to write model card to database", e);
			throw new RuntimeException(e);
		}
		return resp;
	}
}
