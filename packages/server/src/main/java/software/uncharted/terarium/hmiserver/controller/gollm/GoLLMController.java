package software.uncharted.terarium.hmiserver.controller.gollm;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.annotations.IgnoreRequestLogging;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest.TaskType;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.DatasetService;
import software.uncharted.terarium.hmiserver.service.data.DocumentAssetService;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
import software.uncharted.terarium.hmiserver.service.tasks.CompareModelResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.ConfigureFromDatasetResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.ConfigureModelResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.ModelCardResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.TaskService;
import software.uncharted.terarium.hmiserver.service.tasks.TaskService.TaskMode;

@RequestMapping("/gollm")
@RestController
@Slf4j
@RequiredArgsConstructor
public class GoLLMController {

	final private ObjectMapper objectMapper;
	final private TaskService taskService;
	final private DocumentAssetService documentAssetService;
	final private DatasetService datasetService;
	final private ModelService modelService;

	final private ModelCardResponseHandler modelCardResponseHandler;
	final private ConfigureModelResponseHandler configureModelResponseHandler;
	final private CompareModelResponseHandler compareModelResponseHandler;
	final private ConfigureFromDatasetResponseHandler configureFromDatasetResponseHandler;

	@PostConstruct
	void init() {
		taskService.addResponseHandler(modelCardResponseHandler);
		taskService.addResponseHandler(configureModelResponseHandler);
		taskService.addResponseHandler(compareModelResponseHandler);
		taskService.addResponseHandler(configureFromDatasetResponseHandler);
	}

	@PostMapping("/model-card")
	@Secured(Roles.USER)
	@Operation(summary = "Dispatch a `GoLLM Model Card` task")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Dispatched successfully", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TaskResponse.class))),
			@ApiResponse(responseCode = "400", description = "The provided document text is too long", content = @Content),
			@ApiResponse(responseCode = "404", description = "The provided model or document arguments are not found", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue dispatching the request", content = @Content)
	})
	public ResponseEntity<TaskResponse> createModelCardTask(
			@RequestParam(name = "document-id", required = true) final UUID documentId,
			@RequestParam(name = "mode", required = false, defaultValue = "async") final TaskMode mode) {

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

			// check for input length
			if (document.get().getText().length() > 600000) {
				log.warn("Document {} text too long for GoLLM model card task", documentId);
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document text is too long");
			}

			final ModelCardResponseHandler.Input input = new ModelCardResponseHandler.Input();
			input.setResearchPaper(document.get().getText());

			// Create the task
			final TaskRequest req = new TaskRequest();
			req.setType(TaskType.GOLLM);
			req.setScript(ModelCardResponseHandler.NAME);
			req.setInput(objectMapper.writeValueAsBytes(input));

			final ModelCardResponseHandler.Properties props = new ModelCardResponseHandler.Properties();
			props.setDocumentId(documentId);
			req.setAdditionalProperties(props);

			// send the request
			return ResponseEntity.ok().body(taskService.runTask(mode, req));

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
			@RequestParam(name = "document-id", required = true) final UUID documentId,
			@RequestParam(name = "mode", required = false, defaultValue = "async") final TaskMode mode) {

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

			final ConfigureModelResponseHandler.Input input = new ConfigureModelResponseHandler.Input();
			input.setResearchPaper(document.get().getText());
			input.setAmr(model.get());

			// Create the task
			final TaskRequest req = new TaskRequest();
			req.setType(TaskType.GOLLM);
			req.setScript(ConfigureModelResponseHandler.NAME);
			req.setInput(objectMapper.writeValueAsBytes(input));

			final ConfigureModelResponseHandler.Properties props = new ConfigureModelResponseHandler.Properties();
			props.setDocumentId(documentId);
			props.setModelId(modelId);
			req.setAdditionalProperties(props);

			// send the request
			return ResponseEntity.ok().body(taskService.runTask(mode, req));

		} catch (final Exception e) {
			final String error = "Unable to dispatch task request";
			log.error("Unable to dispatch task request {}: {}", error, e.getMessage());
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@PostMapping("/configure-from-dataset")
	@Secured(Roles.USER)
	@Operation(summary = "Dispatch a `GoLLM Config from Dataset` task")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Dispatched successfully", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TaskResponse.class))),
			@ApiResponse(responseCode = "400", description = "The provided document text is too long", content = @Content),
			@ApiResponse(responseCode = "404", description = "The provided model or document arguments are not found", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue dispatching the request", content = @Content)
	})
	public ResponseEntity<TaskResponse> createConfigFromDatasetTask(
			@RequestParam(name = "model-id", required = true) final UUID modelId,
			@RequestParam(name = "dataset-ids", required = true) final List<UUID> datasetIds,
			@RequestParam(name = "mode", required = false, defaultValue = "async") final TaskMode mode) {

		try {

			// Grab the datasets
			final List<String> datasets = new ArrayList<>();
			for (final UUID datasetId : datasetIds) {
				final Optional<Dataset> dataset = datasetService.getAsset(datasetId);
				if (dataset.isEmpty()) {
					throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset not found");
				}

				// make sure there is text in the document
				if (dataset.get().getFileNames() == null || dataset.get().getFileNames().isEmpty()) {
					log.warn("Dataset {} has no source files to send", datasetId);
					throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset has no filenames");
				}

				for (final String filename : dataset.get().getFileNames()) {
					try {
						final Optional<String> datasetText = datasetService.fetchFileAsString(datasetId, filename);
						if (dataset.isPresent()) {
							// ensure unescaped newlines are escaped
							datasets.add(
									datasetText.get().replaceAll("(?<!\\\\)\\n", Matcher.quoteReplacement("\\\\n")));
						}
					} catch (final Exception e) {
						throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to fetch file for dataset");
					}
				}
			}

			if (datasets.isEmpty()) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No datasets found");
			}

			// Grab the model
			final Optional<Model> model = modelService.getAsset(modelId);
			if (model.isEmpty()) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Model not found");
			}

			final ConfigureFromDatasetResponseHandler.Input input = new ConfigureFromDatasetResponseHandler.Input();
			input.setDatasets(datasets);
			input.setAmr(model.get());

			// Create the task
			final TaskRequest req = new TaskRequest();
			req.setType(TaskType.GOLLM);
			req.setScript(ConfigureFromDatasetResponseHandler.NAME);
			req.setInput(objectMapper.writeValueAsBytes(input));

			final ConfigureFromDatasetResponseHandler.Properties props = new ConfigureFromDatasetResponseHandler.Properties();
			props.setDatasetIds(datasetIds);
			props.setModelId(modelId);
			req.setAdditionalProperties(props);

			// send the request
			return ResponseEntity.ok().body(taskService.runTask(mode, req));

		} catch (final Exception e) {
			final String error = "Unable to dispatch task request";
			log.error("Unable to dispatch task request {}: {}", error, e.getMessage());
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@GetMapping("/compare-model")
	@Secured(Roles.USER)
	@Operation(summary = "Dispatch a `GoLLM Compare Model` task")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Dispatched successfully", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TaskResponse.class))),
			@ApiResponse(responseCode = "404", description = "The provided model or document arguments are not found", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue dispatching the request", content = @Content)
	})
	public ResponseEntity<TaskResponse> creatCompareModelTask(
			@RequestParam(name = "model-ids", required = true) final List<UUID> modelIds,
			@RequestParam(name = "mode", required = false, defaultValue = "async") final TaskMode mode) {
		try {
			final List<JsonNode> modelCards = new ArrayList<>();
			for (final UUID modelId : modelIds) {
				// Grab the model
				final Optional<Model> model = modelService.getAsset(modelId);
				if (model.isEmpty()) {
					throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Model not found");
				}
				if (model.get().getMetadata().getGollmCard() != null) {
					final JsonNode modelCard = model.get().getMetadata().getGollmCard();
					modelCards.add(modelCard);
				}
			}

			final CompareModelResponseHandler.Input input = new CompareModelResponseHandler.Input();
			input.setModelCards(modelCards);

			// Create the task
			final TaskRequest req = new TaskRequest();
			req.setType(TaskType.GOLLM);
			req.setScript(CompareModelResponseHandler.NAME);
			req.setInput(objectMapper.writeValueAsBytes(input));

			// send the request
			return ResponseEntity.ok().body(taskService.runTask(mode, req));

		} catch (final Exception e) {
			final String error = "Unable to dispatch task request";
			log.error("Unable to dispatch task request {}: {}", error, e.getMessage());
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
