package software.uncharted.terarium.hmiserver.controller.mirac;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.annotations.IgnoreRequestLogging;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.models.task.TaskStatus;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.TaskService;
import software.uncharted.terarium.hmiserver.service.TaskService.TaskType;
import software.uncharted.terarium.hmiserver.service.data.ModelService;

@RequestMapping("/mirac")
@RestController
@Slf4j
@RequiredArgsConstructor
public class MiraCController {

	final private ObjectMapper objectMapper;
	final private TaskService taskService;
	final private ModelService modelService;

	final private String STELLA_TO_STOCKFLOW = "mirac:stella_to_stockflow";
	final private String MDL_TO_STOCKFLOW = "mirac:mdl_to_stockflow";
	final private String SBML_TO_PETRINET = "mirac:sbml_to_petrinet";
	final private String PETRINET_FORMAT = "petrinet";
	final private String STOCKFLOW_FORMAT = "strickflow";
	static final private long REQUEST_TIMEOUT_SECONDS = 30;

	@Data
	static public class ModelConversionRequest {
		public String modelName;
		public String modelContent;
	};

	@PostMapping("/convert_and_create_model")
	@Secured(Roles.USER)
	@Operation(summary = "Dispatch a mira conversion task")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Dispatched successfully and model created", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TaskResponse.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue dispatching the request", content = @Content)
	})
	public ResponseEntity<Model> convertAndCreateModel(
			@RequestBody final ModelConversionRequest conversionRequest) {

		try {

			TaskRequest req = new TaskRequest();
			req.setInput(conversionRequest.getModelContent().getBytes());

			String resultingFormat = "";

			if (conversionRequest.getModelName().endsWith(".mdl")) {
				req.setScript(MDL_TO_STOCKFLOW);
				resultingFormat = STOCKFLOW_FORMAT;
			} else if (conversionRequest.getModelName().endsWith(".stella")) {
				req.setScript(STELLA_TO_STOCKFLOW);
				resultingFormat = STOCKFLOW_FORMAT;
			} else if (conversionRequest.getModelName().endsWith(".sbml")) {
				req.setScript(SBML_TO_PETRINET);
				resultingFormat = PETRINET_FORMAT;
			} else {
				throw new ResponseStatusException(
						org.springframework.http.HttpStatus.BAD_REQUEST,
						"Unknown model type");
			}

			List<TaskResponse> responses = taskService.runTaskBlocking(req, TaskType.MIRAC,
					REQUEST_TIMEOUT_SECONDS);

			TaskResponse resp = responses.get(responses.size() - 1);

			if (resp.getStatus() != TaskStatus.SUCCESS) {
				throw new ResponseStatusException(
						org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
						"Unable to generate vectors for knn search");
			}

			byte[] outputBytes = resp.getOutput();
			JsonNode output = objectMapper.readTree(outputBytes);

			Model model = objectMapper.convertValue(output, Model.class);

			return ResponseEntity.status(HttpStatus.CREATED).body(modelService.createAsset(model));

		} catch (final Exception e) {
			final String error = "Unable to dispatch task request";
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@GetMapping("/configure-model")
	@Secured(Roles.USER)
	@Operation(summary = "Dispatch a `GoLLM Configure Model` task")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Dispatched successfully", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TaskResponse.class))),
			@ApiResponse(responseCode = "404", description = "The provided model or document arguments are not found", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue dispatching the request", content = @Content)
	})
	public ResponseEntity<TaskResponse> createConfigureModelTask(
			@RequestParam(name = "model-id", required = true) final UUID modelId,
			@RequestParam(name = "document-id", required = true) final UUID documentId) {

		try {

			// Grab the document
			final Optional<DocumentAsset> document = documentAssetService.getAsset(documentId);
			if (document.isEmpty()) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found");
			}

			// make sure there is text in the document
			if (document.get().getText() == null || document.get().getText().isEmpty()) {
				log.warn("Document {} has no text to send", documentId);
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document has no text");
			}

			// Grab the model
			final Optional<Model> model = modelService.getAsset(modelId);
			if (model.isEmpty()) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Model not found");
			}

			final ConfigureModelInput input = new ConfigureModelInput();
			input.setResearchPaper(document.get().getText());
			input.setAmr(model.get());

			// Create the task
			final TaskRequest req = new TaskRequest();
			req.setId(java.util.UUID.randomUUID());
			req.setScript(CONFIGURE_MODEL_SCRIPT);
			req.setInput(objectMapper.writeValueAsBytes(input));

			final ConfigureModelProperties props = new ConfigureModelProperties();
			props.setDocumentId(documentId);
			props.setModelId(modelId);
			req.setAdditionalProperties(props);

			// send the request
			taskService.sendTaskRequest(req, TaskType.GOLLM);

			final TaskResponse resp = req.createResponse(TaskStatus.QUEUED);
			return ResponseEntity.ok().body(resp);

		} catch (final Exception e) {
			final String error = "Unable to dispatch task request";
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@PutMapping("/{task-id}")
	@Operation(summary = "Cancel a GoLLM task")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Dispatched cancellation successfully", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Void.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue dispatching the cancellation", content = @Content)
	})
	public ResponseEntity<Void> cancelTask(@PathVariable("task-id") final UUID taskId) {
		taskService.cancelTask(taskId);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{task-id}")
	@Operation(summary = "Subscribe for updates on a GoLLM task")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Subscribed successfully", content = @Content(mediaType = "text/event-stream", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TaskResponse.class))),
	})
	@IgnoreRequestLogging
	public SseEmitter subscribe(@PathVariable("task-id") final UUID taskId) {
		return taskService.subscribe(taskId);
	}
}
