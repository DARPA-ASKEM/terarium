package software.uncharted.terarium.hmiserver.controller.knowledge;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.annotations.HasProjectAccess;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest.TaskType;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.models.task.TaskStatus;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.ClientEventService;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.ExtractionService;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
import software.uncharted.terarium.hmiserver.service.notification.NotificationService;
import software.uncharted.terarium.hmiserver.service.tasks.EquationsCleanupResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.LatexToSympyResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.SympyToAMRResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.TaskService;
import software.uncharted.terarium.hmiserver.service.tasks.TaskService.TaskMode;
import software.uncharted.terarium.hmiserver.utils.Messages;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@RequestMapping("/knowledge")
@RestController
@Slf4j
@RequiredArgsConstructor
public class KnowledgeController {

	private final Config config;
	private final ObjectMapper mapper;

	private final ModelService modelService;

	private final ExtractionService extractionService;
	private final TaskService taskService;

	private final CurrentUserService currentUserService;

	private final EquationsCleanupResponseHandler equationsCleanupResponseHandler;

	private final Messages messages;

	@PostConstruct
	void init() {
		taskService.addResponseHandler(equationsCleanupResponseHandler);
	}

	/**
	 * Clean up a list of equations
	 *
	 * @return List of cleaned-up equations
	 */
	@PostMapping("/clean-equations")
	@Secured(Roles.USER)
	public ResponseEntity<EquationCleanupResponse> getCleanedEquations(
		@RequestBody final List<String> equations,
		@RequestParam(name = "project-id", required = false) final UUID projectId
	) {
		TaskRequest cleanupReq = cleanupEquationsTaskRequest(projectId, equations);
		TaskResponse cleanupResp;
		List<String> cleanedEquations = new ArrayList<>(equations); // Have original equations as a fallback
		boolean wasCleaned = false;

		try {
			cleanupResp = taskService.runTask(TaskMode.SYNC, cleanupReq);
			if (cleanupResp.getStatus() != TaskStatus.SUCCESS) {
				log.error("Task failed", cleanupResp.getStderr());
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, cleanupResp.getStderr());
			}
			// Get the equations from the cleanup response
			if (cleanupResp != null && cleanupResp.getOutput() != null) {
				try {
					JsonNode output = mapper.readValue(cleanupResp.getOutput(), JsonNode.class);
					if (output.get("response") != null && output.get("response").get("equations") != null) {
						cleanedEquations.clear(); // Clear original equations before adding cleaned ones
						wasCleaned = true;
						for (JsonNode eq : output.get("response").get("equations")) {
							cleanedEquations.add(eq.asText());
						}
					}
				} catch (IOException e) {
					log.warn("Unable to retrieve cleaned-up equations from GoLLM response. Reverting to original equations.", e);
				}
			}
		} catch (final JsonProcessingException e) {
			log.warn("Unable to clean-up equations due to a JsonProcessingException. Reverting to original equations.", e);
		} catch (final TimeoutException e) {
			log.warn("Unable to clean-up equations due to a TimeoutException. Reverting to original equations.", e);
		} catch (final InterruptedException e) {
			log.warn("Unable to clean-up equations due to a InterruptedException. Reverting to original equations.", e);
		} catch (final ExecutionException e) {
			log.warn("Unable to clean-up equations due to a ExecutionException. Reverting to original equations.", e);
		}

		return ResponseEntity.ok(new EquationCleanupResponse(cleanedEquations, wasCleaned));
	}

	@Data
	public static class NotificationProperties {

		private UUID projectId;
		private UUID modelId;
		private UUID documentId;
		private UUID workflowId;
		private UUID nodeId;
	}

	@Data
	static class EquationRef {

		private String id;
		private String equationStr;
	}

	@Data
	static class EquationToModelInput {

		private UUID nodeId;
		private UUID workflowId;
		private Map<UUID, List<EquationRef>> equationsWithSource;
		private List<String> equations;
	}

	@PostMapping("/equations-to-model")
	@Secured(Roles.USER)
	@HasProjectAccess(level = Schema.Permission.WRITE)
	public ResponseEntity<UUID> equationsToModelNew(
		@RequestBody final EquationToModelInput req,
		@RequestParam(name = "project-id", required = false) final UUID projectId
	) {
		// Collect equations
		List<String> equations = new ArrayList();
		Map<UUID, List<String>> modelProvenance = new HashMap();

		if (req.getEquationsWithSource() != null) {
			for (var entry : req.getEquationsWithSource().entrySet()) {
				List<String> refs = new ArrayList();
				for (var equationRef : entry.getValue()) {
					equations.add(equationRef.getEquationStr());
					refs.add(equationRef.getId());
				}
				modelProvenance.put(entry.getKey(), refs);
			}
		}
		if (req.getEquations() != null) {
			for (var equationStr : req.getEquations()) {
				equations.add(equationStr);
			}
		}

		final TaskRequest latexToSympyRequest;
		final TaskResponse latexToSympyResponse;
		final TaskRequest sympyToAMRRequest;
		final TaskResponse sympyToAMRResponse;
		final Model responseAMR;

		try {
			// 1. LaTeX to sympy code
			latexToSympyRequest = createLatexToSympyTask(mapper.valueToTree(equations));
			latexToSympyResponse = taskService.runTaskSync(latexToSympyRequest);

			if (latexToSympyResponse.getStatus() != TaskStatus.SUCCESS) {
				log.error("Task Failed", latexToSympyResponse.getStderr());
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, latexToSympyResponse.getStderr());
			}

			// 2. hand off
			final String code = extractCodeFromLatexToSympy(latexToSympyResponse);

			// 3. sympy code string to amr json
			sympyToAMRRequest = createSympyToAMRTask(code);
			sympyToAMRResponse = taskService.runTaskSync(sympyToAMRRequest);
			if (sympyToAMRResponse.getStatus() != TaskStatus.SUCCESS) {
				log.error("Task Failed", sympyToAMRResponse.getStderr());
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, sympyToAMRResponse.getStderr());
			}

			final JsonNode taskResponseJSON = mapper.readValue(sympyToAMRResponse.getOutput(), JsonNode.class);
			final ObjectNode amrNode = taskResponseJSON.get("response").get("amr").deepCopy();
			responseAMR = mapper.convertValue(amrNode, Model.class);
		} catch (final Exception e) {
			log.error(messages.get("task.mira.internal-error"), e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.mira.internal-error"));
		}

		// Inject provenance information into metadata and save model
		if (responseAMR != null) {
			responseAMR.getMetadata().setModelProvenance(modelProvenance);
		}

		// Save model
		final Model model;
		try {
			model = modelService.createAsset(responseAMR, projectId);
		} catch (final IOException e) {
			log.error("An error occurred while trying to create a model.", e);
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, messages.get("postgres.service-unavailable"));
		}

		return ResponseEntity.ok(model.getId());
	}

	/**
	 * Document Extractions
	 *
	 * @param documentId (String): The ID of the document to profile
	 * @return an accepted response, the request being handled asynchronously
	 */
	@PostMapping("/pdf-extractions")
	@Secured(Roles.USER)
	@Operation(summary = "Extracts information from the first PDF associated with the given document id")
	@HasProjectAccess(level = Schema.Permission.WRITE)
	@ApiResponses(
		value = {
			@ApiResponse(responseCode = "202", description = "Extraction started on PDF", content = @Content),
			@ApiResponse(responseCode = "500", description = "Error running PDF extraction", content = @Content)
		}
	)
	public ResponseEntity<Void> pdfExtractions(
		@RequestParam("document-id") final UUID documentId,
		@RequestParam(name = "project-id", required = false) final UUID projectId,
		@RequestParam(name = "mode", required = false, defaultValue = "ASYNC") final TaskMode mode
	) {
		final Future<DocumentAsset> newF = extractionService.extractPDFAndApplyToDocumentNew(documentId, projectId);
		if (mode == TaskMode.SYNC) {
			try {
				newF.get();
			} catch (Exception e) {
				e.printStackTrace();
				log.error("Error extracting Document", e);
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("document.extraction.failed"));
			}
		}
		return ResponseEntity.accepted().build();
	}

	@Data
	public static class SympyCode {

		private String code;
	}

	@PostMapping("/sympy-code-to-amr")
	@Secured(Roles.USER)
	@Operation(summary = "Generate AMR from sympy python code, DEBUGGING only")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Dispatched successfully",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TaskResponse.class)
				)
			),
			@ApiResponse(responseCode = "500", description = "There was an issue dispatching the request", content = @Content)
		}
	)
	public ResponseEntity<JsonNode> sympyCodeToAMRDebug(@RequestBody final SympyCode code) {
		final TaskRequest sympyToAMRRequest;
		TaskResponse sympyToAMRResponse = null;
		final JsonNode response;

		try {
			sympyToAMRRequest = createSympyToAMRTask(code.getCode());
			sympyToAMRResponse = taskService.runTaskSync(sympyToAMRRequest);
			response = mapper.readValue(sympyToAMRResponse.getOutput(), JsonNode.class);

			if (sympyToAMRResponse.getStatus() != TaskStatus.SUCCESS) {
				log.error("Task Failed", sympyToAMRResponse.getStderr());
				ObjectNode objectNode = mapper.createObjectNode();
				objectNode.put("error", sympyToAMRResponse.getStderr());
				return ResponseEntity.ok().body(objectNode);
			}
			return ResponseEntity.ok().body(response);
		} catch (final TimeoutException e) {
			log.warn("Timeout while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, messages.get("task.mira.timeout"));
		} catch (final InterruptedException e) {
			log.warn("Interrupted while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.mira.interrupted"));
		} catch (final ExecutionException e) {
			log.error("Error while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.mira.execution-failure"));
		} catch (final Exception e) {
			log.error("Unexpected error", e);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messages.get("generic.io-error.read"));
		}
	}

	@PostMapping("/equations-to-model-debug")
	@Secured(Roles.USER)
	@Operation(summary = "Generate AMR from latex ODE equations, DEBUGGING only")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Dispatched successfully",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TaskResponse.class)
				)
			),
			@ApiResponse(responseCode = "500", description = "There was an issue dispatching the request", content = @Content)
		}
	)
	/**
	 * This is similar to /equations-to-model endpoint, but rather than
	 * directly creating a model asset it returns artifact at different intersections
	 * and handoff for debugging
	 **/
	public ResponseEntity<JsonNode> latexToAMRDebug(@RequestBody final String latex) {
		////////////////////////////////////////////////////////////////////////////////
		// 1. Convert latex string to python sympy code string
		//
		// Note this is a gollm string => string task
		////////////////////////////////////////////////////////////////////////////////
		final TaskRequest latexToSympyRequest;
		final TaskResponse latexToSympyResponse;
		String code = null;

		try {
			JsonNode temp = mapper.readValue(latex, JsonNode.class);
			latexToSympyRequest = createLatexToSympyTask(temp);
			latexToSympyResponse = taskService.runTaskSync(latexToSympyRequest);
			if (latexToSympyResponse.getStatus() != TaskStatus.SUCCESS) {
				log.error("Task Failed", latexToSympyResponse.getStderr());
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, latexToSympyResponse.getStderr());
			}
			code = extractCodeFromLatexToSympy(latexToSympyResponse);
		} catch (final TimeoutException e) {
			log.warn("Timeout while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, messages.get("task.gollm.timeout"));
		} catch (final InterruptedException e) {
			log.warn("Interrupted while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.gollm.interrupted"));
		} catch (final ExecutionException e) {
			log.error("Error while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.gollm.execution-failure"));
		} catch (final Exception e) {
			log.error("Unexpected error", e);
		}

		if (code == null) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.gollm.execution-failure"));
		}

		////////////////////////////////////////////////////////////////////////////////
		// 2. Convert python sympy code string to amr
		//
		// This returns the AMR json, and intermediate data representations for debugging
		////////////////////////////////////////////////////////////////////////////////
		final TaskRequest sympyToAMRRequest;
		final TaskResponse sympyToAMRResponse;
		final JsonNode response;

		try {
			sympyToAMRRequest = createSympyToAMRTask(code);
			sympyToAMRResponse = taskService.runTaskSync(sympyToAMRRequest);
			if (sympyToAMRResponse.getStatus() != TaskStatus.SUCCESS) {
				log.error("Task Failed", sympyToAMRResponse.getStderr());
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, sympyToAMRResponse.getStderr());
			}
			response = mapper.readValue(sympyToAMRResponse.getOutput(), JsonNode.class);
			return ResponseEntity.ok().body(response);
		} catch (final TimeoutException e) {
			log.warn("Timeout while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, messages.get("task.mira.timeout"));
		} catch (final InterruptedException e) {
			log.warn("Interrupted while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.mira.interrupted"));
		} catch (final ExecutionException e) {
			log.error("Error while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.mira.execution-failure"));
		} catch (final Exception e) {
			log.error("Unexpected error", e);
		}
		throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.io-error.read"));
	}

	private TaskRequest cleanupEquationsTaskRequest(UUID projectId, List<String> equations) {
		final EquationsCleanupResponseHandler.Input input = new EquationsCleanupResponseHandler.Input();
		input.setLlm(config.getLlm());
		input.setEquations(equations);

		// Create the task
		final TaskRequest req = new TaskRequest();
		req.setType(TaskType.GOLLM);
		req.setScript(EquationsCleanupResponseHandler.NAME);
		req.setUserId(currentUserService.get().getId());

		try {
			req.setInput(mapper.writeValueAsBytes(input));
		} catch (final Exception e) {
			log.error("Unable to serialize input", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.io-error.write"));
		}

		req.setProjectId(projectId);

		final EquationsCleanupResponseHandler.Properties props = new EquationsCleanupResponseHandler.Properties();
		props.setProjectId(projectId);
		req.setAdditionalProperties(props);

		return req;
	}

	// Helper to create sympy => AMR task
	private TaskRequest createSympyToAMRTask(final String sympyCode) throws Exception {
		final TaskRequest sympyToAMRRequest = new TaskRequest();
		sympyToAMRRequest.setType(TaskType.MIRA);
		sympyToAMRRequest.setInput(sympyCode.getBytes());
		sympyToAMRRequest.setScript(SympyToAMRResponseHandler.NAME);
		sympyToAMRRequest.setUserId(currentUserService.get().getId());
		return sympyToAMRRequest;
	}

	// Helper to create latex => sympy task
	private TaskRequest createLatexToSympyTask(JsonNode equationsNode) throws Exception {
		final TaskRequest latexToSympyRequest = new TaskRequest();

		final LatexToSympyResponseHandler.Input input = new LatexToSympyResponseHandler.Input();
		input.setLlm(config.getLlm());
		final List<String> equationList = new ArrayList<>();
		for (final JsonNode equation : equationsNode) {
			equationList.add(equation.asText());
		}
		input.setEquations(equationList);
		latexToSympyRequest.setType(TaskType.GOLLM);
		latexToSympyRequest.setInput(mapper.writeValueAsBytes(input));
		latexToSympyRequest.setScript(LatexToSympyResponseHandler.NAME);
		latexToSympyRequest.setUserId(currentUserService.get().getId());
		latexToSympyRequest.setUseCache(false); // Don't cache because LLM can give incorrect result
		return latexToSympyRequest;
	}

	private String extractCodeFromLatexToSympy(final TaskResponse taskResponse) throws Exception {
		final LatexToSympyResponseHandler.Response response = mapper.readValue(
			taskResponse.getOutput(),
			LatexToSympyResponseHandler.Response.class
		);
		StringBuilder codeBuilder = new StringBuilder();
		if (response.getResponse().get("equations").isArray()) {
			for (JsonNode codeNode : response.getResponse().get("equations")) {
				codeBuilder.append(codeNode.asText()).append("\n");
			}
		}
		return codeBuilder.toString().trim();
	}

	private static class EquationCleanupResponse {

		public List<String> cleanedEquations;
		public boolean wasCleaned;

		public EquationCleanupResponse(List<String> cleanedEquations, boolean wasCleaned) {
			this.cleanedEquations = cleanedEquations;
			this.wasCleaned = wasCleaned;
		}
	}
}
