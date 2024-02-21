package software.uncharted.terarium.hmiserver.controller.search;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.ErrorCause;
import co.elastic.clients.elasticsearch._types.KnnQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.models.task.TaskStatus;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.TaskService;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

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

	@PostMapping("/{asset-type}")
	@Secured(Roles.USER)
	@Operation(summary = "Executes a knn search against the provided asset type")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Query results", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = JsonNode.class))),
			@ApiResponse(responseCode = "204", description = "There was no concept found", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving the concept from the data store", content = @Content)
	})
	public ResponseEntity<List<JsonNode>> searchByAssetType(
			@PathVariable("asset-type") final AssetType assetType,
			@RequestParam(value = "text", required = true) final String text,
			@RequestParam(value = "k", defaultValue = "10") final int k,
			@RequestParam(value = "num-results", defaultValue = "100") final int numResults,
			@RequestParam(value = "num-candidates", defaultValue = "100") final int numCandidates,
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

			// sha256 the text to use as a cache key
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hash = md.digest(text.getBytes(StandardCharsets.UTF_8));

			// check if we already have the vectors cached
			List<Float> vector = queryVectorCache.get(hash);
			if (vector == null) {

				// set the embedding model

				GoLLMSearchRequest embeddingRequest = new GoLLMSearchRequest();
				embeddingRequest.setText(text);
				embeddingRequest.setEmbeddingModel(EMBEDDING_MODEL);

				TaskRequest req = new TaskRequest();
				req.setInput(embeddingRequest);
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

			KnnQuery knn = new KnnQuery.Builder()
					.field("embeddings.vector")
					.queryVector(vector)
					.k(k)
					.numCandidates(numCandidates)
					.build();

			Query query = new Query.Builder()
					.bool(b -> b
							.mustNot(mn -> mn.exists(e -> e.field("deletedOn")))
							.mustNot(mn -> mn.term(t -> t.field("temporary").value(true))))
					.build();

			SearchResponse<JsonNode> res = esService.knnSearch(index, knn, query, numResults, JsonNode.class);

			final List<JsonNode> docs = new ArrayList<>();
			for (final Hit<JsonNode> hit : res.hits().hits()) {
				ObjectNode source = (ObjectNode) hit.source();
				if (source != null) {
					source.put("id", hit.id());
					docs.add(source);
				}
			}

			return ResponseEntity.ok(docs);

		} catch (ElasticsearchException e) {
			String error = "Unable to get execute knn search: " + e.response().error().reason();
			ErrorCause causedBy = e.response().error().causedBy();
			if (causedBy != null) {
				error += ", caused by: " + causedBy.reason();
			}
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		} catch (Exception e) {

			final String error = "Unable to get execute knn search";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}

	}

}
