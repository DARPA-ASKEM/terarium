package software.uncharted.terarium.hmiserver.controller.gollm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest.TaskType;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.data.DatasetService;
import software.uncharted.terarium.hmiserver.service.data.DocumentAssetService;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;
import software.uncharted.terarium.hmiserver.service.tasks.CompareModelsResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.ConfigureFromDatasetResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.ConfigureModelResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.GenerateSummaryHandler;
import software.uncharted.terarium.hmiserver.service.tasks.ModelCardResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.TaskService;
import software.uncharted.terarium.hmiserver.service.tasks.TaskService.TaskMode;
import software.uncharted.terarium.hmiserver.utils.Messages;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@RequestMapping("/gollm")
@RestController
@Slf4j
@RequiredArgsConstructor
public class GoLLMController {

	private final ObjectMapper objectMapper;
	private final TaskService taskService;
	private final DocumentAssetService documentAssetService;
	private final DatasetService datasetService;
	private final ModelService modelService;
	private final ProjectService projectService;
	private final CurrentUserService currentUserService;

	private final ModelCardResponseHandler modelCardResponseHandler;
	private final ConfigureModelResponseHandler configureModelResponseHandler;
	private final CompareModelsResponseHandler compareModelsResponseHandler;
	private final ConfigureFromDatasetResponseHandler configureFromDatasetResponseHandler;
	private final GenerateSummaryHandler generateSummaryHandler;

	private final Messages messages;

	@PostConstruct
	void init() {
		taskService.addResponseHandler(modelCardResponseHandler);
		taskService.addResponseHandler(configureModelResponseHandler);
		taskService.addResponseHandler(compareModelsResponseHandler);
		taskService.addResponseHandler(configureFromDatasetResponseHandler);
		taskService.addResponseHandler(generateSummaryHandler);
	}

	@PostMapping("/model-card")
	@Secured(Roles.USER)
	@Operation(summary = "Dispatch a `GoLLM Model Card` task")
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
			@ApiResponse(responseCode = "400", description = "The provided document text is too long", content = @Content),
			@ApiResponse(
				responseCode = "404",
				description = "The provided model or document arguments are not found",
				content = @Content
			),
			@ApiResponse(responseCode = "500", description = "There was an issue dispatching the request", content = @Content)
		}
	)
	public ResponseEntity<TaskResponse> createModelCardTask(
		@RequestParam(name = "document-id", required = true) final UUID documentId,
		@RequestParam(name = "mode", required = false, defaultValue = "ASYNC") final TaskMode mode,
		@RequestParam(name = "project-id", required = false) final UUID projectId
	) {
		final Schema.Permission permission = projectService.checkPermissionCanRead(
			currentUserService.get().getId(),
			projectId
		);

		// Grab the document
		final Optional<DocumentAsset> documentOpt = documentAssetService.getAsset(documentId, permission);
		if (documentOpt.isEmpty()) {
			log.warn(String.format("Document %s not found", documentId));
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, messages.get("document.not-found"));
		}

		final DocumentAsset document = documentOpt.get();

		// make sure there is text in the document
		if (document.getText() == null || document.getText().isEmpty()) {
			log.warn(String.format("Document %s has no text to send", documentId));
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, messages.get("document.extraction.not-done"));
		}

		// check for input length
		if (document.getText().length() > ModelCardResponseHandler.MAX_TEXT_SIZE) {
			log.warn(String.format("Document %s text too long for GoLLM model card task", documentId));
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messages.get("document.text-length-exceeded"));
		}

		final ModelCardResponseHandler.Input input = new ModelCardResponseHandler.Input();
		input.setResearchPaper(document.getText());

		// Create the task
		final TaskRequest req = new TaskRequest();
		req.setType(TaskType.GOLLM);
		req.setScript(ModelCardResponseHandler.NAME);
		req.setUserId(currentUserService.get().getId());

		try {
			req.setInput(objectMapper.writeValueAsBytes(input));
		} catch (final Exception e) {
			log.error("Unable to serialize input", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.io-error.write"));
		}

		req.setProjectId(projectId);

		final ModelCardResponseHandler.Properties props = new ModelCardResponseHandler.Properties();
		props.setProjectId(projectId);
		props.setDocumentId(documentId);
		req.setAdditionalProperties(props);

		final TaskResponse resp;
		try {
			resp = taskService.runTask(mode, req);
		} catch (final JsonProcessingException e) {
			log.error("Unable to serialize input", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.gollm.json-processing"));
		} catch (final TimeoutException e) {
			log.warn("Timeout while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, messages.get("task.gollm.timeout"));
		} catch (final InterruptedException e) {
			log.warn("Interrupted while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, messages.get("task.gollm.interrupted"));
		} catch (final ExecutionException e) {
			log.error("Error while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.gollm.execution-failure"));
		}

		// send the response
		return ResponseEntity.ok().body(resp);
	}

	@GetMapping("/configure-model")
	@Secured(Roles.USER)
	@Operation(summary = "Dispatch a `GoLLM Configure Model` task")
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
			@ApiResponse(
				responseCode = "404",
				description = "The provided model or document arguments are not found",
				content = @Content
			),
			@ApiResponse(responseCode = "500", description = "There was an issue dispatching the request", content = @Content)
		}
	)
	public ResponseEntity<TaskResponse> createConfigureModelTask(
		@RequestParam(name = "model-id", required = true) final UUID modelId,
		@RequestParam(name = "document-id", required = true) final UUID documentId,
		@RequestParam(name = "mode", required = false, defaultValue = "ASYNC") final TaskMode mode,
		@RequestParam(name = "workflow-id", required = false) final UUID workflowId,
		@RequestParam(name = "node-id", required = false) final UUID nodeId,
		@RequestParam(name = "project-id", required = false) final UUID projectId
	) {
		final Schema.Permission permission = projectService.checkPermissionCanRead(
			currentUserService.get().getId(),
			projectId
		);

		// Grab the document
		final Optional<DocumentAsset> document = documentAssetService.getAsset(documentId, permission);
		if (document.isEmpty()) {
			log.warn(String.format("Document %s not found", documentId));
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, messages.get("document.not-found"));
		}

		// make sure there is text in the document
		if (document.get().getText() == null || document.get().getText().isEmpty()) {
			log.warn(String.format("Document %s has no extracted text", documentId));
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, messages.get("document.extraction.not-done"));
		}

		// Grab the model
		final Optional<Model> model = modelService.getAsset(modelId, permission);
		if (model.isEmpty()) {
			log.warn(String.format("Model %s not found", modelId));
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, messages.get("model.not-found"));
		}

		final ConfigureModelResponseHandler.Input input = new ConfigureModelResponseHandler.Input();
		input.setResearchPaper(document.get().getText());
		// stripping the metadata from the model before its sent since it can cause
		// gollm to fail with massive inputs
		model.get().setMetadata(null);
		input.setAmr(model.get().serializeWithoutTerariumFieldsKeepId());

		// Create the task
		final TaskRequest req = new TaskRequest();
		req.setType(TaskType.GOLLM);
		req.setScript(ConfigureModelResponseHandler.NAME);
		req.setUserId(currentUserService.get().getId());

		try {
			req.setInput(objectMapper.writeValueAsBytes(input));
		} catch (final Exception e) {
			log.error("Unable to serialize input", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.io-error.write"));
		}

		req.setProjectId(projectId);

		final ConfigureModelResponseHandler.Properties props = new ConfigureModelResponseHandler.Properties();
		props.setProjectId(projectId);
		props.setDocumentId(documentId);
		props.setModelId(modelId);
		props.setWorkflowId(workflowId);
		props.setNodeId(nodeId);
		req.setAdditionalProperties(props);

		final TaskResponse resp;
		try {
			resp = taskService.runTask(mode, req);
		} catch (final JsonProcessingException e) {
			log.error("Unable to serialize input", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.gollm.json-processing"));
		} catch (final TimeoutException e) {
			log.warn("Timeout while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, messages.get("task.gollm.timeout"));
		} catch (final InterruptedException e) {
			log.warn("Interrupted while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, messages.get("task.gollm.interrupted"));
		} catch (final ExecutionException e) {
			log.error("Error while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.gollm.execution-failure"));
		}

		return ResponseEntity.ok().body(resp);
	}

	@Data
	public static class ConfigFromDatasetBody {

		private String matrixStr = "";
	}

	@PostMapping("/configure-from-dataset")
	@Secured(Roles.USER)
	@Operation(summary = "Dispatch a `GoLLM Config from Dataset` task")
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
			@ApiResponse(responseCode = "400", description = "The provided document text is too long", content = @Content),
			@ApiResponse(
				responseCode = "404",
				description = "The provided model or document arguments are not found",
				content = @Content
			),
			@ApiResponse(responseCode = "500", description = "There was an issue dispatching the request", content = @Content)
		}
	)
	public ResponseEntity<TaskResponse> createConfigFromDatasetTask(
		@RequestParam(name = "model-id", required = true) final UUID modelId,
		@RequestParam(name = "dataset-ids", required = true) final List<UUID> datasetIds,
		@RequestParam(name = "mode", required = false, defaultValue = "ASYNC") final TaskMode mode,
		@RequestParam(name = "workflow-id", required = false) final UUID workflowId,
		@RequestParam(name = "node-id", required = false) final UUID nodeId,
		@RequestParam(name = "project-id", required = false) final UUID projectId,
		@RequestBody(required = false) final ConfigFromDatasetBody body
	) {
		final Schema.Permission permission = projectService.checkPermissionCanRead(
			currentUserService.get().getId(),
			projectId
		);

		// Grab the datasets
		final List<String> datasets = new ArrayList<>();
		for (final UUID datasetId : datasetIds) {
			final Optional<Dataset> dataset = datasetService.getAsset(datasetId, permission);
			if (dataset.isEmpty()) {
				log.warn(String.format("Dataset %s not found", datasetId));
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, messages.get("dataset.not-found"));
			}

			// make sure there is text in the document
			if (dataset.get().getFileNames() == null || dataset.get().getFileNames().isEmpty()) {
				log.warn(String.format("Dataset %s has no source files to send", datasetId));
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, messages.get("dataset.files.not-found"));
			}

			for (final String filename : dataset.get().getFileNames()) {
				try {
					final Optional<String> datasetText = datasetService.fetchFileAsString(datasetId, filename);
					if (datasetText.isPresent()) {
						// ensure unescaped newlines are escaped
						datasets.add(datasetText.get().replaceAll("(?<!\\\\)\\n", Matcher.quoteReplacement("\\\\n")));
					}
				} catch (final Exception e) {
					log.warn("Unable to fetch dataset files", e);
					throw new ResponseStatusException(HttpStatus.NOT_FOUND, messages.get("dataset.files.not-found"));
				}
			}
		}

		if (datasets.isEmpty()) {
			log.warn("No datasets found");
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, messages.get("dataset.not-found"));
		}

		// Grab the model
		final Optional<Model> model = modelService.getAsset(modelId, permission);
		if (model.isEmpty()) {
			log.warn(String.format("Model %s not found", modelId));
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, messages.get("model.not-found"));
		}

		final ConfigureFromDatasetResponseHandler.Input input = new ConfigureFromDatasetResponseHandler.Input();
		input.setDatasets(datasets);
		// stripping the metadata from the model before its sent since it can cause
		// gollm to fail with massive inputs
		model.get().setMetadata(null);
		input.setAmr(model.get().serializeWithoutTerariumFields());

		// set matrix string if provided
		if (body != null && !body.getMatrixStr().isEmpty()) {
			input.setMatrixStr(body.getMatrixStr());
		}

		// Create the task
		final TaskRequest req = new TaskRequest();
		req.setType(TaskType.GOLLM);
		req.setScript(ConfigureFromDatasetResponseHandler.NAME);
		req.setUserId(currentUserService.get().getId());

		try {
			req.setInput(objectMapper.writeValueAsBytes(input));
		} catch (final Exception e) {
			log.error("Unable to serialize input", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.io-error.write"));
		}

		req.setProjectId(projectId);

		final ConfigureFromDatasetResponseHandler.Properties props = new ConfigureFromDatasetResponseHandler.Properties();
		props.setProjectId(projectId);
		props.setDatasetIds(datasetIds);
		props.setModelId(modelId);
		props.setWorkflowId(workflowId);
		props.setNodeId(nodeId);
		req.setAdditionalProperties(props);

		final TaskResponse resp;
		try {
			resp = taskService.runTask(mode, req);
		} catch (final JsonProcessingException e) {
			log.error("Unable to serialize input", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.gollm.json-processing"));
		} catch (final TimeoutException e) {
			log.warn("Timeout while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, messages.get("task.gollm.timeout"));
		} catch (final InterruptedException e) {
			log.warn("Interrupted while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, messages.get("task.gollm.interrupted"));
		} catch (final ExecutionException e) {
			log.error("Error while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.gollm.execution-failure"));
		}

		return ResponseEntity.ok().body(resp);
	}

	@GetMapping("/compare-models")
	@Secured(Roles.USER)
	@Operation(summary = "Dispatch a `GoLLM Compare Models` task")
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
			@ApiResponse(
				responseCode = "404",
				description = "The provided model arguments are not found",
				content = @Content
			),
			@ApiResponse(responseCode = "500", description = "There was an issue dispatching the request", content = @Content)
		}
	)
	public ResponseEntity<TaskResponse> createCompareModelsTask(
		@RequestParam(name = "model-ids", required = true) final List<UUID> modelIds,
		@RequestParam(name = "mode", required = false, defaultValue = "ASYNC") final TaskMode mode,
		@RequestParam(name = "workflow-id", required = false) final UUID workflowId,
		@RequestParam(name = "node-id", required = false) final UUID nodeId,
		@RequestParam(name = "project-id", required = false) final UUID projectId
	) {
		final Schema.Permission permission = projectService.checkPermissionCanWrite(
			currentUserService.get().getId(),
			projectId
		);

		final List<String> amrs = new ArrayList<>();
		for (final UUID modelId : modelIds) {
			// Grab the model
			final Optional<Model> model = modelService.getAsset(modelId, permission);
			if (model.isEmpty()) {
				log.warn(String.format("Model %s not found", modelId));
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, messages.get("model.not-found"));
			}

			amrs.add(model.get().serializeWithoutTerariumFields());
		}

		// if the number of models is less than 2, return an error
		if (amrs.size() < 2) {
			log.warn("Less than 2 models provided for comparison");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messages.get("task.gollm.model-card.bad-number"));
		}

		final CompareModelsResponseHandler.Input input = new CompareModelsResponseHandler.Input();
		input.setAmrs(amrs);

		// create the task
		final TaskRequest req = new TaskRequest();
		req.setType(TaskType.GOLLM);
		req.setScript(CompareModelsResponseHandler.NAME);
		req.setUserId(currentUserService.get().getId());

		try {
			req.setInput(objectMapper.writeValueAsBytes(input));
		} catch (final JsonProcessingException e) {
			log.error("Unable to serialize input", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.io-error.write"));
		}

		req.setProjectId(projectId);

		final CompareModelsResponseHandler.Properties props = new CompareModelsResponseHandler.Properties();
		props.setWorkflowId(workflowId);
		props.setNodeId(nodeId);
		req.setAdditionalProperties(props);

		final TaskResponse resp;
		try {
			resp = taskService.runTask(mode, req);
		} catch (final JsonProcessingException e) {
			log.error("Unable to serialize input", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.gollm.json-processing"));
		} catch (final TimeoutException e) {
			log.warn("Timeout while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, messages.get("task.gollm.timeout"));
		} catch (final InterruptedException e) {
			log.warn("Interrupted while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, messages.get("task.gollm.interrupted"));
		} catch (final ExecutionException e) {
			log.error("Error while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.gollm.execution-failure"));
		}

		return ResponseEntity.ok().body(resp);
	}

	@PostMapping("/generate-summary")
	@Secured(Roles.USER)
	@Operation(summary = "Dispatch a `GoLLM Generate Summary` task")
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
			@ApiResponse(
				responseCode = "422",
				description = "The request was interrupted while waiting for a response",
				content = @Content
			),
			@ApiResponse(
				responseCode = "503",
				description = "The request was timed out while waiting for a response",
				content = @Content
			),
			@ApiResponse(responseCode = "500", description = "There was an issue dispatching the request", content = @Content)
		}
	)
	public ResponseEntity<TaskResponse> createGenerateResponseTask(
		@RequestParam(name = "mode", required = false, defaultValue = "SYNC") final TaskMode mode,
		@RequestParam(name = "previousSummaryId", required = false) final UUID previousSummaryId,
		@RequestParam(name = "project-id", required = false) final UUID projectId,
		@RequestBody final String instruction
	) {
		// create the task
		final TaskRequest req = new TaskRequest();
		req.setType(TaskType.GOLLM);
		req.setScript(GenerateSummaryHandler.NAME);
		req.setUserId(currentUserService.get().getId());

		try {
			req.setInput(instruction.getBytes(StandardCharsets.UTF_8));
		} catch (final JsonProcessingException e) {
			log.error("Unable to serialize input: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.io-error.write"));
		}

		req.setProjectId(projectId);

		final GenerateSummaryHandler.Properties props = new GenerateSummaryHandler.Properties();
		props.setProjectId(projectId);
		props.setSummaryId(UUID.randomUUID());
		props.setPreviousSummaryId(previousSummaryId);
		req.setAdditionalProperties(props);

		final TaskResponse resp;
		try {
			resp = taskService.runTask(mode, req);
		} catch (final JsonProcessingException e) {
			log.error("Unable to serialize input: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.gollm.json-processing"));
		} catch (final TimeoutException e) {
			log.error("Timeout while waiting for task response: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, messages.get("task.gollm.timeout"));
		} catch (final InterruptedException e) {
			log.error("Interrupted while waiting for task response: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, messages.get("task.gollm.interrupted"));
		} catch (final ExecutionException e) {
			log.error("Error while waiting for task response: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.gollm.execution-failure"));
		}

		return ResponseEntity.ok().body(resp);
	}

	@PutMapping("/{task-id}")
	@Operation(summary = "Cancel a GoLLM task")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Dispatched cancellation successfully",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Void.class)
				)
			),
			@ApiResponse(
				responseCode = "500",
				description = "There was an issue dispatching the cancellation",
				content = @Content
			)
		}
	)
	public ResponseEntity<Void> cancelTask(@PathVariable("task-id") final UUID taskId) {
		taskService.cancelTask(taskId);
		return ResponseEntity.ok().build();
	}
}
