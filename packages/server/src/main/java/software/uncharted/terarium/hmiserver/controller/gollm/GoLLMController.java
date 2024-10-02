package software.uncharted.terarium.hmiserver.controller.gollm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.PostConstruct;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javax.imageio.ImageIO;
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
import software.uncharted.terarium.hmiserver.models.dataservice.document.ExtractedDocumentPage;
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
import software.uncharted.terarium.hmiserver.service.tasks.ConfigureModelFromDatasetResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.ConfigureModelFromDocumentResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.EnrichAmrResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.EquationsFromImageResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.GenerateResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.GenerateSummaryHandler;
import software.uncharted.terarium.hmiserver.service.tasks.InterventionsFromDocumentResponseHandler;
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

	private final CompareModelsResponseHandler compareModelsResponseHandler;
	private final ConfigureModelFromDatasetResponseHandler configureModelFromDatasetResponseHandler;
	private final ConfigureModelFromDocumentResponseHandler configureModelFromDocumentResponseHandler;
	private final EnrichAmrResponseHandler enrichAmrResponseHandler;
	private final EquationsFromImageResponseHandler equationsFromImageResponseHandler;
	private final GenerateResponseHandler generateResponseHandler;
	private final GenerateSummaryHandler generateSummaryHandler;
	private final InterventionsFromDocumentResponseHandler interventionsFromDocumentResponseHandler;
	private final ModelCardResponseHandler modelCardResponseHandler;

	private final Messages messages;

	@PostConstruct
	void init() {
		taskService.addResponseHandler(compareModelsResponseHandler);
		taskService.addResponseHandler(configureModelFromDatasetResponseHandler);
		taskService.addResponseHandler(configureModelFromDocumentResponseHandler);
		taskService.addResponseHandler(enrichAmrResponseHandler);
		taskService.addResponseHandler(equationsFromImageResponseHandler);
		taskService.addResponseHandler(generateResponseHandler);
		taskService.addResponseHandler(generateSummaryHandler);
		taskService.addResponseHandler(interventionsFromDocumentResponseHandler);
		taskService.addResponseHandler(modelCardResponseHandler);
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
		@RequestParam(name = "model-id", required = true) final UUID modelId,
		@RequestParam(name = "document-id", required = false) final UUID documentId,
		@RequestParam(name = "mode", required = false, defaultValue = "ASYNC") final TaskMode mode,
		@RequestParam(name = "project-id", required = false) final UUID projectId
	) {
		final Schema.Permission permission = projectService.checkPermissionCanRead(
			currentUserService.get().getId(),
			projectId
		);

		// Grab the model
		final Optional<Model> model = modelService.getAsset(modelId, permission);
		if (model.isEmpty()) {
			log.warn(String.format("Model %s not found", modelId));
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, messages.get("model.not-found"));
		}

		final ModelCardResponseHandler.Input input = new ModelCardResponseHandler.Input();
		input.setAmr(model.get().serializeWithoutTerariumFields(null, new String[] { "gollmCard" }));

		// Grab the document
		final DocumentAsset document;
		if (documentId != null) {
			final Optional<DocumentAsset> documentOpt = documentAssetService.getAsset(documentId, permission);
			if (documentOpt.isEmpty()) {
				log.warn(String.format("Document %s not found", documentId));
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, messages.get("document.not-found"));
			}

			document = documentOpt.get();

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

			input.setResearchPaper(document.getText());
		}

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
		props.setModelId(modelId);
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
	public ResponseEntity<TaskResponse> createConfigureModelFromDocumentTask(
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

		final ConfigureModelFromDocumentResponseHandler.Input input = new ConfigureModelFromDocumentResponseHandler.Input();

		String text = "";
		if (document.get().getExtractions().size() > 0) {
			for (final ExtractedDocumentPage page : document.get().getExtractions()) {
				text += page.getText() + "\n";
				if (page.getTables() != null) {
					for (final JsonNode table : page.getTables()) {
						text += table.toString() + "\n";
					}
				}
				if (page.getEquations() != null) {
					for (final JsonNode equation : page.getEquations()) {
						text += equation.toString() + "\n";
					}
				}
			}
		} else {
			text = document.get().getText();
		}

		input.setResearchPaper(text);

		// stripping the metadata from the model before its sent since it can cause
		// gollm to fail with massive inputs
		model.get().setMetadata(null);
		input.setAmr(model.get().serializeWithoutTerariumFields(new String[] { "id" }, null));

		// Create the task
		final TaskRequest req = new TaskRequest();
		req.setType(TaskType.GOLLM);
		req.setScript(ConfigureModelFromDocumentResponseHandler.NAME);
		req.setUserId(currentUserService.get().getId());

		try {
			req.setInput(objectMapper.writeValueAsBytes(input));
		} catch (final Exception e) {
			log.error("Unable to serialize input", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.io-error.write"));
		}

		req.setProjectId(projectId);

		final ConfigureModelFromDocumentResponseHandler.Properties props =
			new ConfigureModelFromDocumentResponseHandler.Properties();
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
	public ResponseEntity<TaskResponse> createConfigureModelFromDatasetTask(
		@RequestParam(name = "model-id", required = true) final UUID modelId,
		@RequestParam(name = "dataset-id", required = true) final UUID datasetId,
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

		// Grab the model
		final Optional<Model> model = modelService.getAsset(modelId, permission);
		if (model.isEmpty()) {
			log.warn(String.format("Model %s not found", modelId));
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, messages.get("model.not-found"));
		}

		// Grab the dataset
		final List<String> dataArray = new ArrayList<>();

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
					final List<String> rows = Arrays.asList(datasetText.get().split("\\r?\\n"));
					dataArray.addAll(rows);
				}
			} catch (final Exception e) {
				log.warn("Unable to fetch dataset files", e);
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, messages.get("dataset.files.not-found"));
			}
		}

		final ConfigureModelFromDatasetResponseHandler.Input input = new ConfigureModelFromDatasetResponseHandler.Input();
		input.setDataset(dataArray);
		// stripping the metadata from the model before its sent since it can cause
		// gollm to fail with massive inputs
		model.get().setMetadata(null);
		input.setAmr(model.get().serializeWithoutTerariumFields(null, null));

		// set matrix string if provided
		if (body != null && !body.getMatrixStr().isEmpty()) {
			input.setMatrix(body.getMatrixStr());
		}

		// Create the task
		final TaskRequest req = new TaskRequest();
		req.setType(TaskType.GOLLM);
		req.setScript(ConfigureModelFromDatasetResponseHandler.NAME);
		req.setUserId(currentUserService.get().getId());

		try {
			req.setInput(objectMapper.writeValueAsBytes(input));
		} catch (final Exception e) {
			log.error("Unable to serialize input", e);
		}

		req.setProjectId(projectId);

		final ConfigureModelFromDatasetResponseHandler.Properties props =
			new ConfigureModelFromDatasetResponseHandler.Properties();
		props.setProjectId(projectId);
		props.setDatasetId(datasetId);
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

	@GetMapping("/interventions-from-document")
	@Secured(Roles.USER)
	@Operation(summary = "Dispatch a `GoLLM interventions-from-document` task")
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
	public ResponseEntity<TaskResponse> createInterventionsFromDocumentTask(
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

		final InterventionsFromDocumentResponseHandler.Input input = new InterventionsFromDocumentResponseHandler.Input();
		input.setResearchPaper(document.get().getText());

		// stripping the metadata from the model before its sent since it can cause
		// gollm to fail with massive inputs
		model.get().setMetadata(null);
		input.setAmr(model.get().serializeWithoutTerariumFields(new String[] { "id" }, null));

		// Create the task
		final TaskRequest req = new TaskRequest();
		req.setType(TaskType.GOLLM);
		req.setScript(InterventionsFromDocumentResponseHandler.NAME);
		req.setUserId(currentUserService.get().getId());

		try {
			req.setInput(objectMapper.writeValueAsBytes(input));
		} catch (final Exception e) {
			log.error("Unable to serialize input", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.io-error.write"));
		}

		req.setProjectId(projectId);

		final InterventionsFromDocumentResponseHandler.Properties props =
			new InterventionsFromDocumentResponseHandler.Properties();
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

			amrs.add(model.get().serializeWithoutTerariumFields(null, null));
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

	@GetMapping("/enrich-amr")
	@Secured(Roles.USER)
	@Operation(summary = "Dispatch a `GoLLM Enrich AMR` task")
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
	public ResponseEntity<TaskResponse> createEnrichAMRTask(
		@RequestParam(name = "model-id", required = true) final UUID modelId,
		@RequestParam(name = "document-id", required = true) final UUID documentId,
		@RequestParam(name = "mode", required = false, defaultValue = "ASYNC") final TaskMode mode,
		@RequestParam(name = "project-id", required = false) final UUID projectId,
		@RequestParam(name = "overwrite", required = false, defaultValue = "false") final boolean overwrite
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

		final EnrichAmrResponseHandler.Input input = new EnrichAmrResponseHandler.Input();
		input.setResearchPaper(document.get().getText());
		// stripping the metadata from the model before its sent since it can cause
		// gollm to fail with massive inputs
		model.get().setMetadata(null);

		try {
			final String amr = objectMapper.writeValueAsString(model.get());
			input.setAmr(amr);
		} catch (final JsonProcessingException e) {
			log.error("Unable to serialize model card", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.gollm.json-processing"));
		}

		// Create the task
		final TaskRequest req = new TaskRequest();
		req.setType(TaskType.GOLLM);
		req.setScript(EnrichAmrResponseHandler.NAME);
		req.setUserId(currentUserService.get().getId());

		try {
			req.setInput(objectMapper.writeValueAsBytes(input));
		} catch (final Exception e) {
			log.error("Unable to serialize input", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.io-error.write"));
		}

		req.setProjectId(projectId);

		final EnrichAmrResponseHandler.Properties props = new EnrichAmrResponseHandler.Properties();
		props.setProjectId(projectId);
		props.setDocumentId(documentId);
		props.setModelId(modelId);
		props.setOverwrite(overwrite);
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
	public static class EquationsFromImageBody {

		private String base64ImageStr = "";
	}

	@PostMapping("/equations-from-image")
	@Secured(Roles.USER)
	@Operation(summary = "Dispatch a `GoLLM Equations from image` task")
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
			@ApiResponse(responseCode = "400", description = "The provided image is invalid", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue dispatching the request", content = @Content)
		}
	)
	public ResponseEntity<TaskResponse> equationsFromImageTask(
		@RequestParam(name = "project-id", required = false) final UUID projectId,
		@RequestParam(name = "document-id", required = true) final UUID documentId,
		@RequestParam(name = "mode", required = false, defaultValue = "ASYNC") final TaskMode mode,
		@RequestBody final EquationsFromImageBody image
	) {
		final Schema.Permission permission = projectService.checkPermissionCanRead(
			currentUserService.get().getId(),
			projectId
		);

		// validate that the string is a base64 encoding
		byte[] decodedImage;
		try {
			decodedImage = Base64.getDecoder().decode(image.base64ImageStr);
		} catch (final IllegalArgumentException e) {
			log.error("Invalid base64 encoding for image", e);
			throw new ResponseStatusException(
				HttpStatus.BAD_REQUEST,
				messages.get("task.gollm.equations-from-image.invalid-image")
			);
		}

		// validate that the image is a valid image
		try (ByteArrayInputStream bais = new ByteArrayInputStream(decodedImage)) {
			final BufferedImage bi = ImageIO.read(bais);
		} catch (final IOException e) {
			log.error("Invalid image provided", e);
			throw new ResponseStatusException(
				HttpStatus.BAD_REQUEST,
				messages.get("task.gollm.equations-from-image.invalid-image")
			);
		}

		final EquationsFromImageResponseHandler.Input input = new EquationsFromImageResponseHandler.Input();
		input.setImage(image.base64ImageStr);

		// Create the task
		final TaskRequest req = new TaskRequest();
		req.setType(TaskType.GOLLM);
		req.setScript(EquationsFromImageResponseHandler.NAME);
		req.setUserId(currentUserService.get().getId());

		try {
			req.setInput(objectMapper.writeValueAsBytes(input));
		} catch (final Exception e) {
			log.error("Unable to serialize input", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.io-error.write"));
		}

		req.setProjectId(projectId);

		final EquationsFromImageResponseHandler.Properties props = new EquationsFromImageResponseHandler.Properties();
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

		return ResponseEntity.ok().body(resp);
	}

	@PostMapping("/generate-response")
	@Secured(Roles.USER)
	@Operation(summary = "Dispatch a `GoLLM Generate Response` task.")
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
		@RequestParam(name = "project-id", required = false) final UUID projectId,
		@Parameter(
			name = "response-format",
			description = "The format of the response, either 'json' or OpenAI response_format json object, if not provided, the response will be in the default text format",
			schema = @io.swagger.v3.oas.annotations.media.Schema(
				oneOf = { JsonNode.class, String.class },
				allowableValues = { "json" }
			),
			in = ParameterIn.QUERY
		) @RequestParam(name = "response-format", required = false) final Object responseFormat,
		@RequestBody final String instruction
	) {
		JsonNode resFormat = null;

		if (responseFormat instanceof JsonNode) {
			resFormat = (JsonNode) responseFormat;
		} else if (responseFormat instanceof final String format) {
			if (format.equals("json")) {
				try {
					resFormat = objectMapper.readTree("{\"type\": \"json_object\"}");
				} catch (final JsonProcessingException e) {
					throw new IllegalArgumentException("Invalid JSON format for response-format parameter");
				}
			}
		}

		// set task input
		final GenerateResponseHandler.Input input = new GenerateResponseHandler.Input();
		input.setInstruction(instruction);
		input.setResponseFormat(resFormat);

		// create the task
		final TaskRequest req = new TaskRequest();
		req.setType(TaskType.GOLLM);
		req.setScript(GenerateResponseHandler.NAME);
		req.setUserId(currentUserService.get().getId());

		try {
			req.setInput(objectMapper.writeValueAsBytes(input));
		} catch (final JsonProcessingException e) {
			log.error("Unable to serialize input", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.io-error.write"));
		}

		req.setProjectId(projectId);

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
