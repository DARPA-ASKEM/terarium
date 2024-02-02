package software.uncharted.terarium.hmiserver.controller.knn;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.elastic.clients.elasticsearch._types.KnnQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.models.task.TaskStatus;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.TaskService;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

@RequestMapping("/knn")
@RestController
@Slf4j
@RequiredArgsConstructor
public class KNNSearchController {

	final private ObjectMapper objectMapper;
	final private TaskService taskService;
	final private RedissonClient redissonClient;
	final private ElasticsearchService elasticsearchService;
	private RMapCache<byte[], List<Float>> queryVectorCache;

	final private long CACHE_TTL_SECONDS = 60 * 60 * 24;
	final private long REQUEST_TIMEOUT_SECONDS = 10;
	final private String EMBEDDING_MODEL = "text-embedding-ada-002";
	final private int NUM_RESULTS = 10;
	final private int NUM_CANDIDATES = 10;

	@Data
	static public class KNNSearchRequest {
		private String text;
		@JsonProperty("embedding_model")
		private String embeddingModel;
	}

	@Data
	private static class EmbeddingsResponse {
		List<Float> response;
	}

	@PostConstruct
	public void init() {
		queryVectorCache = redissonClient.getMapCache("knn-vector-cache");
	}

	@GetMapping("/{index}")
	@Secured(Roles.USER)
	@Operation(summary = "Executes a knn search against provided index")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Query results", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = JsonNode.class))),
			@ApiResponse(responseCode = "204", description = "There was no concept found", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving the concept from the data store", content = @Content)
	})
	public ResponseEntity<List<JsonNode>> knnSearch(
			@PathVariable("index") final String index,
			@RequestBody KNNSearchRequest body) {

		try {
			// sha256 the text to use as a cache key
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hash = md.digest(body.getText().getBytes(StandardCharsets.UTF_8));

			// check if we already have the vectors cached
			List<Float> vector = queryVectorCache.get(hash);
			if (vector == null) {

				// set the embedding model
				body.setEmbeddingModel(EMBEDDING_MODEL);

				TaskRequest req = new TaskRequest();
				req.setInput(body);
				req.setScript("gollm:embedding");

				List<TaskResponse> responses = taskService.runTaskBlocking(req, REQUEST_TIMEOUT_SECONDS);

				TaskResponse resp = responses.get(responses.size() - 1);

				if (resp.getStatus() != TaskStatus.SUCCESS) {
					throw new ResponseStatusException(
							org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
							"Unable to generate vectors for knn search");
				}

				byte[] outputBytes = resp.getOutput();
				JsonNode output = objectMapper.readTree(outputBytes);

				EmbeddingsResponse embeddingResp = objectMapper.convertValue(output, EmbeddingsResponse.class);

				vector = embeddingResp.getResponse();

				// store the vectors in the cache
				queryVectorCache.put(hash, vector, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
			}

			KnnQuery query = new KnnQuery.Builder().field("paragraphs.vector").queryVector(vector)
					.k(NUM_RESULTS).numCandidates(NUM_CANDIDATES)
					.build();

			List<JsonNode> docs = elasticsearchService.knnSearch(index, query, JsonNode.class);

			return ResponseEntity.ok(docs);
		} catch (Exception e) {
			final String error = "Unable to get execute knn search";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

}
