package software.uncharted.terarium.hmiserver.controller.search;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.ErrorCause;
import co.elastic.clients.elasticsearch._types.KnnQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.models.task.TaskStatus;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.TaskService;
import software.uncharted.terarium.hmiserver.service.TaskService.TaskType;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RequestMapping("/search-by-asset-type")
@RestController
@Slf4j
@RequiredArgsConstructor
public class SearchByAssetTypeController {

	final private ObjectMapper objectMapper;
	final private TaskService taskService;
	final private RedissonClient redissonClient;
	final private ElasticsearchService esService;
	final private ElasticsearchConfiguration esConfig;
	private RMapCache<byte[], List<Float>> queryVectorCache;

	static final private long CACHE_TTL_SECONDS = 60 * 60 * 2; // 2 hours
	static final private long REQUEST_TIMEOUT_SECONDS = 30;
	static final private String EMBEDDING_MODEL = "text-embedding-ada-002";
	static final private String REDIS_EMBEDDING_CACHE_KEY = "knn-vector-cache";

	static final private List<String> EXCLUDE_FIELDS = List.of("embeggings", "text", "topics");

	@Data
	static public class GoLLMSearchRequest {
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
		queryVectorCache = redissonClient.getMapCache(REDIS_EMBEDDING_CACHE_KEY);
	}

	@GetMapping("/{asset-type}")
	@Secured(Roles.USER)
	@Operation(summary = "Executes a knn search against the provided asset type")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Query results", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = JsonNode.class))),
			@ApiResponse(responseCode = "204", description = "There was no concept found", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving the concept from the data store", content = @Content)
	})
	public ResponseEntity<List<JsonNode>> searchByAssetType(
			@PathVariable("asset-type") final AssetType assetType,
			@RequestParam(value = "page-size", defaultValue = "100", required = false) final Integer pageSize,
			@RequestParam(value = "page", defaultValue = "0", required = false) final Integer page,
			@RequestParam(value = "text", defaultValue = "") final String text,
			@RequestParam(value = "k", defaultValue = "100") final int k,
			@RequestParam(value = "num-candidates", defaultValue = "1000") final int numCandidates,
			@RequestParam(value = "embedding-model", defaultValue = EMBEDDING_MODEL) final String embeddingModel,
			@RequestParam(value = "index", defaultValue = "") String index) {
		try {

			if (index.equals("")) {
				index = esConfig.getIndex(assetType.toString().toLowerCase());
			}

			if (!esService.containsIndex(index)) {
				log.error("Unsupported asset type: {}, index {} does not exist", assetType, index);
				return ResponseEntity.badRequest().build();
			}

			if (k > numCandidates) {
				return ResponseEntity.badRequest().build();
			}

			KnnQuery knn = null;
			if (text != null && !text.isEmpty()) {
				// sha256 the text to use as a cache key
				final MessageDigest md = MessageDigest.getInstance("SHA-256");
				final byte[] hash = md.digest(text.getBytes(StandardCharsets.UTF_8));

				// check if we already have the vectors cached
				List<Float> vector = queryVectorCache.get(hash);
				if (vector == null) {

					// set the embedding model

					final GoLLMSearchRequest embeddingRequest = new GoLLMSearchRequest();
					embeddingRequest.setText(text);
					embeddingRequest.setEmbeddingModel(EMBEDDING_MODEL);

					final TaskRequest req = new TaskRequest();
					req.setInput(embeddingRequest);
					req.setScript("gollm:embedding");

					final List<TaskResponse> responses = taskService.runTaskBlocking(req, TaskType.GOLLM,
							REQUEST_TIMEOUT_SECONDS);

					final TaskResponse resp = responses.get(responses.size() - 1);

					if (resp.getStatus() != TaskStatus.SUCCESS) {
						throw new ResponseStatusException(
								org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
								"Unable to generate vectors for knn search");
					}

					final byte[] outputBytes = resp.getOutput();
					final JsonNode output = objectMapper.readTree(outputBytes);

					final EmbeddingsResponse embeddingResp = objectMapper.convertValue(output, EmbeddingsResponse.class);

					vector = embeddingResp.getResponse();

					// store the vectors in the cache
					queryVectorCache.put(hash, vector, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
				}

				knn = new KnnQuery.Builder()
						.field("embeddings.vector")
						.queryVector(vector)
						.k(k)
						.numCandidates(numCandidates)
						.build();
			}

			final Query query = new Query.Builder()
					.bool(b -> b
							.mustNot(mn -> mn.exists(e -> e.field("deletedOn")))
							.mustNot(mn -> mn.term(t -> t.field("temporary").value(true))))
					.build();

			final SearchResponse<JsonNode> res = esService.knnSearch(index, knn, query, page, pageSize, EXCLUDE_FIELDS,
					JsonNode.class);

			final List<JsonNode> docs = new ArrayList<>();
			for (final Hit<JsonNode> hit : res.hits().hits()) {
				final ObjectNode source = (ObjectNode) hit.source();
				if (source != null) {
					source.put("id", hit.id());
					docs.add(source);
				}
			}

			return ResponseEntity.ok(docs);

		} catch (final ElasticsearchException e) {
			String error = "Unable to get execute knn search: " + e.response().error().reason();
			final ErrorCause causedBy = e.response().error().causedBy();
			if (causedBy != null) {
				error += ", caused by: " + causedBy.reason();
			}
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		} catch (final Exception e) {

			final String error = "Unable to get execute knn search";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}

	}

}
