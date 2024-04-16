package software.uncharted.terarium.hmiserver.controller.search;

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
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest.TaskType;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.models.task.TaskStatus;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.tasks.TaskService;

@RequestMapping("/search-by-asset-type")
@RestController
@Slf4j
@RequiredArgsConstructor
public class SearchByAssetTypeController {

	private final ObjectMapper objectMapper;
	private final TaskService taskService;
	private final ElasticsearchService esService;
	private final ElasticsearchConfiguration esConfig;

	private static final long REQUEST_TIMEOUT_SECONDS = 30;
	private static final String EMBEDDING_MODEL = "text-embedding-ada-002";

	private static final List<String> EXCLUDE_FIELDS = List.of("embeddings", "text", "topics");

	@Data
	public static class GoLLMSearchRequest {
		private String text;

		@JsonProperty("embedding_model")
		private String embeddingModel;
	}

	@Data
	private static class EmbeddingsResponse {
		List<Float> response;
	}

	@GetMapping("/{asset-type}")
	@Secured(Roles.USER)
	@Operation(summary = "Executes a knn search against the provided asset type")
	@ApiResponses(
			value = {
				@ApiResponse(
						responseCode = "200",
						description = "Query results",
						content =
								@Content(
										mediaType = "application/json",
										schema =
												@io.swagger.v3.oas.annotations.media.Schema(
														implementation = JsonNode.class))),
				@ApiResponse(responseCode = "204", description = "There was no concept found", content = @Content),
				@ApiResponse(
						responseCode = "500",
						description = "There was an issue retrieving the concept from the data store",
						content = @Content)
			})
	public ResponseEntity<List<JsonNode>> searchByAssetType(
			@PathVariable("asset-type") final String assetTypeName,
			@RequestParam(value = "page-size", defaultValue = "100", required = false) final Integer pageSize,
			@RequestParam(value = "page", defaultValue = "0", required = false) final Integer page,
			@RequestParam(value = "text", defaultValue = "") final String text,
			@RequestParam(value = "k", defaultValue = "100") final int k,
			@RequestParam(value = "num-candidates", defaultValue = "1000") final int numCandidates,
			@RequestParam(value = "embedding-model", defaultValue = EMBEDDING_MODEL) final String embeddingModel,
			@RequestParam(value = "index", defaultValue = "") String index) {
		final AssetType assetType = AssetType.getAssetType(assetTypeName, objectMapper);
		try {

			if (index.equals("")) {
				index = esConfig.getIndex(assetType.toString().toLowerCase());
			}

			if (!esService.indexExists(index)) {
				log.error("Unsupported asset type: {}, index {} does not exist", assetType, index);
				return ResponseEntity.badRequest().build();
			}

			if (k > numCandidates) {
				return ResponseEntity.badRequest().build();
			}

			KnnQuery knn = null;
			if (text != null && !text.isEmpty()) {

				// create the embedding search request
				final GoLLMSearchRequest embeddingRequest = new GoLLMSearchRequest();
				embeddingRequest.setText(text);
				embeddingRequest.setEmbeddingModel(EMBEDDING_MODEL);

				final TaskRequest req = new TaskRequest();
				req.setType(TaskType.GOLLM);
				req.setInput(embeddingRequest);
				req.setScript("gollm:embedding");

				final TaskResponse resp = taskService.runTaskSync(req, REQUEST_TIMEOUT_SECONDS);

				if (resp.getStatus() != TaskStatus.SUCCESS) {
					throw new ResponseStatusException(
							org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
							"Unable to generate vectors for knn search");
				}

				final byte[] outputBytes = resp.getOutput();
				final JsonNode output = objectMapper.readTree(outputBytes);

				final EmbeddingsResponse embeddingResp = objectMapper.convertValue(output, EmbeddingsResponse.class);

				final List<Float> vector = embeddingResp.getResponse();

				knn = new KnnQuery.Builder()
						.field("embeddings.vector")
						.queryVector(vector)
						.k(k)
						.numCandidates(numCandidates)
						.build();
			}

			final Query query = new Query.Builder()
					.bool(b -> b.mustNot(mn -> mn.exists(e -> e.field("deletedOn")))
							.mustNot(mn -> mn.term(t -> t.field("temporary").value(true))))
					.build();

			final SearchResponse<JsonNode> res =
					esService.knnSearch(index, knn, query, page, pageSize, EXCLUDE_FIELDS, JsonNode.class);

			final List<JsonNode> docs = new ArrayList<>();
			for (final Hit<JsonNode> hit : res.hits().hits()) {
				final ObjectNode source = (ObjectNode) hit.source();
				if (source != null) {
					source.put("id", hit.id());
					docs.add(source);
				}
			}

			return ResponseEntity.ok(docs);

		} catch (final Exception e) {

			final String error = "Unable to get execute knn search";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}
}
