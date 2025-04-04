package software.uncharted.terarium.hmiserver.controller.mira;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.annotations.HasProjectAccess;
import software.uncharted.terarium.hmiserver.models.dataservice.Artifact;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.model.configurations.ModelConfiguration;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest.TaskType;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.models.task.TaskStatus;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.data.ArtifactService;
import software.uncharted.terarium.hmiserver.service.data.ModelConfigurationService;
import software.uncharted.terarium.hmiserver.service.data.ModelConfigurationService.ModelConfigurationUpdate;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
import software.uncharted.terarium.hmiserver.service.tasks.AMRToMMTResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.CompareModelsConceptsResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.GenerateModelLatexResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.MdlToStockflowResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.SbmlToPetrinetResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.SimplifyModelResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.StellaToStockflowResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.TaskService;
import software.uncharted.terarium.hmiserver.utils.Messages;

@RequestMapping("/mira")
@RestController
@Slf4j
@RequiredArgsConstructor
public class MiraController {

	private final ObjectMapper objectMapper;
	private final ArtifactService artifactService;
	private final TaskService taskService;
	private final ModelService modelService;
	private final StellaToStockflowResponseHandler stellaToStockflowResponseHandler;
	private final MdlToStockflowResponseHandler mdlToStockflowResponseHandler;
	private final SbmlToPetrinetResponseHandler sbmlToPetrinetResponseHandler;
	private final CompareModelsConceptsResponseHandler compareModelsConceptsResponseHandler;
	private final CurrentUserService currentUserService;
	private final ModelConfigurationService modelConfigurationService;

	private final Messages messages;

	@Data
	public static class ModelConversionRequest {

		UUID artifactId;
		UUID projectId;
	}

	private static boolean endsWith(final String filename, final List<String> suffixes) {
		for (final String suffix : suffixes) {
			if (filename.endsWith(suffix)) {
				return true;
			}
		}
		return false;
	}

	@Data
	public static class ConversionAdditionalProperties {

		UUID projectId;
		String fileName;
	}

	@PostConstruct
	void init() {
		taskService.addResponseHandler(stellaToStockflowResponseHandler);
		taskService.addResponseHandler(mdlToStockflowResponseHandler);
		taskService.addResponseHandler(sbmlToPetrinetResponseHandler);
		taskService.addResponseHandler(compareModelsConceptsResponseHandler);
	}

	@Data
	public static class CompareModelsConceptsRequest {

		final List<UUID> modelIds;
		final UUID workflowId;
		final UUID nodeId;
	}

	@PostMapping("/compare-models-concepts")
	@Secured(Roles.USER)
	@Operation(summary = "Compare multiple AMR")
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
				responseCode = "400",
				description = "Less than 2 models provided for comparison",
				content = @Content
			),
			@ApiResponse(responseCode = "500", description = "There was an issue dispatching the request", content = @Content)
		}
	)
	public ResponseEntity<JsonNode> compareModelsConcepts(
		@RequestBody final CompareModelsConceptsRequest request,
		@RequestParam(name = "project-id", required = false) final UUID projectId
	) {
		// if the number of models is less than 2, return an error
		if (request.modelIds.size() < 2) {
			log.warn("Less than 2 models provided for comparison");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messages.get("mira.bad-number"));
		}

		// Create the task request
		final TaskRequest taskRequest = new TaskRequest();
		taskRequest.setType(TaskType.MIRA);
		taskRequest.setScript(CompareModelsConceptsResponseHandler.NAME);
		taskRequest.setUserId(currentUserService.get().getId());
		taskRequest.setProjectId(projectId);

		// Set the task request additional properties
		final CompareModelsConceptsResponseHandler.Properties properties =
			new CompareModelsConceptsResponseHandler.Properties();
		properties.setModelIds(request.modelIds);
		properties.setWorkflowId(request.workflowId);
		properties.setNodeId(request.nodeId);
		taskRequest.setAdditionalProperties(properties);

		// Fetch the models
		final List<String> amrs = new ArrayList<>();
		for (final UUID modelId : request.modelIds) {
			final Model model = modelService
				.getAsset(modelId)
				.orElseThrow(() -> {
					log.warn("Model {} not found", modelId);
					return new ResponseStatusException(HttpStatus.NOT_FOUND, messages.get("model.not-found"));
				});

			amrs.add(model.serializeWithoutTerariumFields(null, null));
		}

		// Add the models as task request input
		final CompareModelsConceptsResponseHandler.Input input = new CompareModelsConceptsResponseHandler.Input();
		input.setAmrs(amrs);
		try {
			taskRequest.setInput(objectMapper.writeValueAsBytes(input));
		} catch (final JsonProcessingException e) {
			log.error("Unable to serialize input", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.io-error.write"));
		}

		// Send the task request
		final TaskResponse taskResponse;
		try {
			taskResponse = taskService.runTaskSync(taskRequest);
			if (taskResponse.getStatus() != TaskStatus.SUCCESS) {
				log.error("Task Failed", taskResponse.getStderr());
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, taskResponse.getStderr());
			}
		} catch (final JsonProcessingException e) {
			log.error("Unable to serialize input", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.mira.json-processing"));
		} catch (final TimeoutException e) {
			log.warn("Timeout while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, messages.get("task.mira.timeout"));
		} catch (final InterruptedException e) {
			log.warn("Interrupted while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, messages.get("task.mira.interrupted"));
		} catch (final ExecutionException e) {
			log.error("Error while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.mira.execution-failure"));
		}

		final JsonNode comparisonResult;
		try {
			comparisonResult = objectMapper.readValue(taskResponse.getOutput(), JsonNode.class);
		} catch (final IOException e) {
			log.error("Unable to deserialize output", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.io-error.read"));
		}

		return ResponseEntity.ok().body(comparisonResult);
	}

	@PostMapping("/simplify")
	@Secured(Roles.USER)
	@Operation(summary = "simlify AMR model")
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
	public ResponseEntity<JsonNode> simplifyAMR(@RequestBody final JsonNode model) {
		final TaskRequest req = new TaskRequest();
		req.setType(TaskType.MIRA);

		try {
			req.setInput(objectMapper.treeToValue(model, Model.class).serializeWithoutTerariumFields(null, null).getBytes());
		} catch (final Exception e) {
			log.error("Unable to serialize input", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.io-error.write"));
		}

		req.setScript(SimplifyModelResponseHandler.NAME);
		req.setUserId(currentUserService.get().getId());

		// send the request
		final TaskResponse resp;
		try {
			resp = taskService.runTaskSync(req);
			if (resp.getStatus() != TaskStatus.SUCCESS) {
				log.error("Task Failed", resp.getStderr());
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, resp.getStderr());
			}
		} catch (final JsonProcessingException e) {
			log.error("Unable to serialize input", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.mira.json-processing"));
		} catch (final TimeoutException e) {
			log.warn("Timeout while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, messages.get("task.mira.timeout"));
		} catch (final InterruptedException e) {
			log.warn("Interrupted while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, messages.get("task.mira.interrupted"));
		} catch (final ExecutionException e) {
			log.error("Error while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.mira.execution-failure"));
		}

		final JsonNode result;
		try {
			result = objectMapper.readValue(resp.getOutput(), JsonNode.class);
		} catch (final IOException e) {
			log.error("Unable to deserialize output", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.io-error.read"));
		}
		return ResponseEntity.ok().body(result);
	}

	@PostMapping("/amr-to-mmt")
	@Secured(Roles.USER)
	@Operation(summary = "convert AMR to MIRA model template")
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
	public ResponseEntity<JsonNode> convertAMRtoMMT(@RequestBody final JsonNode model) {
		final TaskRequest req = new TaskRequest();
		req.setType(TaskType.MIRA);

		try {
			req.setInput(objectMapper.treeToValue(model, Model.class).serializeWithoutTerariumFields(null, null).getBytes());
		} catch (final Exception e) {
			log.error("Unable to serialize input", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.io-error.write"));
		}

		req.setScript(AMRToMMTResponseHandler.NAME);
		req.setUserId(currentUserService.get().getId());

		// send the request
		final TaskResponse resp;
		try {
			resp = taskService.runTaskSync(req);
			if (resp.getStatus() != TaskStatus.SUCCESS) {
				log.error("Task Failed", resp.getStderr());
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, resp.getStderr());
			}
		} catch (final JsonProcessingException e) {
			log.error("Unable to serialize input", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.mira.json-processing"));
		} catch (final TimeoutException e) {
			log.warn("Timeout while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, messages.get("task.mira.timeout"));
		} catch (final InterruptedException e) {
			log.warn("Interrupted while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, messages.get("task.mira.interrupted"));
		} catch (final ExecutionException e) {
			log.error("Error while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.mira.execution-failure"));
		}

		final JsonNode mmtInfo;
		try {
			mmtInfo = objectMapper.readValue(resp.getOutput(), JsonNode.class);
		} catch (final IOException e) {
			log.error("Unable to deserialize output", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.io-error.read"));
		}

		return ResponseEntity.ok().body(mmtInfo);
	}

	@PostMapping("/model-to-latex")
	@Secured(Roles.USER)
	@Operation(summary = "Generate latex from a model")
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
	public ResponseEntity<JsonNode> generateModelLatex(@RequestBody final JsonNode model) {
		// create request:
		final TaskRequest req = new TaskRequest();
		req.setType(TaskType.MIRA);

		try {
			req.setInput(objectMapper.treeToValue(model, Model.class).serializeWithoutTerariumFields(null, null).getBytes());
		} catch (final Exception e) {
			log.error("Unable to serialize input", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.io-error.write"));
		}

		req.setScript(GenerateModelLatexResponseHandler.NAME);
		req.setUserId(currentUserService.get().getId());

		// send the request
		final TaskResponse resp;
		try {
			resp = taskService.runTaskSync(req);
			if (resp.getStatus() != TaskStatus.SUCCESS) {
				log.error("Task Failed", resp.getStderr());
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, resp.getStderr());
			}
		} catch (final JsonProcessingException e) {
			log.error("Unable to serialize input", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.mira.json-processing"));
		} catch (final TimeoutException e) {
			log.warn("Timeout while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, messages.get("task.mira.timeout"));
		} catch (final InterruptedException e) {
			log.warn("Interrupted while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, messages.get("task.mira.interrupted"));
		} catch (final ExecutionException e) {
			log.error("Error while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.mira.execution-failure"));
		}

		final JsonNode latexResponse;
		try {
			latexResponse = objectMapper.readValue(resp.getOutput(), JsonNode.class);
		} catch (final IOException e) {
			log.error("Unable to deserialize output", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.io-error.read"));
		}

		return ResponseEntity.ok().body(latexResponse);
	}

	@PostMapping("/convert-and-create-model")
	@Secured(Roles.USER)
	@Operation(summary = "Dispatch a MIRA conversion task")
	@HasProjectAccess("#conversionRequest.getProjectId()")
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
	public ResponseEntity<Model> convertAndCreateModel(
		@RequestBody final ModelConversionRequest conversionRequest,
		@RequestParam(name = "project-id", required = false) final UUID projectId
	) {
		final Optional<Artifact> artifact = artifactService.getAsset(conversionRequest.artifactId);
		if (artifact.isEmpty()) {
			log.error(String.format("Unable to find artifact %s.", conversionRequest.artifactId));
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, messages.get("artifact.not-found"));
		}

		if (artifact.get().getFileNames().isEmpty()) {
			log.error(String.format("The files associated with artifact %s are missing.", conversionRequest.artifactId));
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, messages.get("artifact.files.not-found"));
		}

		final String filename = artifact.get().getFileNames().get(0);

		final Optional<String> fileContents;
		try {
			fileContents = artifactService.fetchFileAsString(conversionRequest.artifactId, filename);
		} catch (final IOException e) {
			log.error(String.format("Unable to read file contents for artifact %s.", conversionRequest.artifactId), e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("artifact.file.unable-to-read"));
		}

		if (fileContents.isEmpty()) {
			log.error(String.format("The file contents for artifact %s is empty.", conversionRequest.artifactId));
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messages.get("artifact.file.content.empty"));
		}

		final ConversionAdditionalProperties additionalProperties = new ConversionAdditionalProperties();
		additionalProperties.setProjectId(projectId);
		additionalProperties.setFileName(filename);

		final TaskRequest req = new TaskRequest();
		req.setType(TaskType.MIRA);

		try {
			req.setInput(fileContents.get().getBytes());
		} catch (final Exception e) {
			log.error("Unable to serialize input", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.io-error.write"));
		}
		req.setAdditionalProperties(additionalProperties);
		req.setUserId(currentUserService.get().getId());

		if (endsWith(filename, List.of(".mdl"))) {
			req.setScript(MdlToStockflowResponseHandler.NAME);
		} else if (endsWith(filename, List.of(".xmile", ".itmx", ".stmx"))) {
			req.setScript(StellaToStockflowResponseHandler.NAME);
		} else if (endsWith(filename, List.of(".sbml", ".xml"))) {
			req.setScript(SbmlToPetrinetResponseHandler.NAME);
		} else {
			log.error(String.format("Unable to determine the artifact type from the supplied filename: %s", filename));
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messages.get("artifact.file-type"));
		}

		// send the request
		final TaskResponse resp;
		try {
			resp = taskService.runTaskSync(req);
			if (resp.getStatus() != TaskStatus.SUCCESS) {
				log.error("Task Failed", resp.getStderr());
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, resp.getStderr());
			}
		} catch (final JsonProcessingException e) {
			log.error("Unable to serialize input", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.mira.json-processing"));
		} catch (final TimeoutException e) {
			log.warn("Timeout while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, messages.get("task.mira.timeout"));
		} catch (final InterruptedException e) {
			log.warn("Interrupted while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, messages.get("task.mira.interrupted"));
		} catch (final ExecutionException e) {
			log.error("Error while waiting for task response", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("task.mira.execution-failure"));
		}

		final Model model;
		try {
			model = objectMapper.readValue(resp.getOutput(), Model.class);
			// create a default configuration
			final ModelConfiguration modelConfiguration = ModelConfigurationService.modelConfigurationFromAMR(
				model,
				new ModelConfigurationUpdate()
			);
			modelConfigurationService.createAsset(modelConfiguration, projectId);
		} catch (final IOException e) {
			log.error("Unable to deserialize output", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, messages.get("generic.io-error.read"));
		}

		return ResponseEntity.ok().body(model);
	}

	@PutMapping("/{task-id}")
	@Operation(summary = "Cancel a Mira task")
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
		taskService.cancelTask(TaskType.MIRA, taskId);
		return ResponseEntity.ok().build();
	}
}
