package software.uncharted.terarium.hmiserver.service.gollm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.TerariumAssetEmbeddings;
import software.uncharted.terarium.hmiserver.models.TerariumAssetEmbeddings.Embeddings;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest.TaskType;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.tasks.TaskService;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmbeddingService {

	private final ObjectMapper objectMapper;
	private final TaskService taskService;
	private final CurrentUserService currentUserService;

	private static final int REQUEST_TIMEOUT_MINUTES = 1;
	public static final String EMBEDDING_MODEL = "text-embedding-ada-002";

	@Data
	public static class GoLLMSearchRequest {

		private String text;

		@JsonProperty("embedding_model")
		private String embeddingModel;
	}

	@Data
	private static class EmbeddingsResponse {

		double[] response;
	}

	public TerariumAssetEmbeddings generateEmbeddings(final String input)
		throws TimeoutException, InterruptedException, ExecutionException, IOException {
		// create the embedding search request
		final GoLLMSearchRequest embeddingRequest = new GoLLMSearchRequest();
		embeddingRequest.setText(input);
		embeddingRequest.setEmbeddingModel(EMBEDDING_MODEL);

		final TaskRequest req = new TaskRequest();
		req.setTimeoutMinutes(REQUEST_TIMEOUT_MINUTES);
		req.setType(TaskType.GOLLM);
		req.setInput(embeddingRequest);
		req.setScript("gollm:embedding");
		try {
			req.setUserId(currentUserService.get().getId());
		} catch (Exception e) {
			log.warn("No user id to associate with embedding request");
		}

		final TaskResponse resp = taskService.runTaskSync(req);

		final byte[] outputBytes = resp.getOutput();
		final JsonNode output = objectMapper.readTree(outputBytes);

		final EmbeddingsResponse embeddingResp = objectMapper.convertValue(output, EmbeddingsResponse.class);

		final Embeddings embeddingChunk = new Embeddings();
		embeddingChunk.setVector(embeddingResp.response);
		embeddingChunk.setEmbeddingId(UUID.randomUUID().toString());
		embeddingChunk.setSpans(new long[] { 0, input.length() });

		final TerariumAssetEmbeddings embeddings = new TerariumAssetEmbeddings();
		embeddings.getEmbeddings().add(embeddingChunk);
		return embeddings;
	}
}
