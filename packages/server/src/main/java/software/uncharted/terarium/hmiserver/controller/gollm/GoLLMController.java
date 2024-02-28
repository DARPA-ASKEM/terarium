package software.uncharted.terarium.hmiserver.controller.gollm;

import java.io.IOException;
import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.annotations.IgnoreRequestLogging;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelParameter;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.Provenance;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceRelationType;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceType;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.models.task.TaskStatus;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.TaskResponseHandler;
import software.uncharted.terarium.hmiserver.service.TaskService;
import software.uncharted.terarium.hmiserver.service.TaskService.TaskType;
import software.uncharted.terarium.hmiserver.service.data.DatasetService;
import software.uncharted.terarium.hmiserver.service.data.DocumentAssetService;
import software.uncharted.terarium.hmiserver.service.data.ModelConfigurationService;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
import software.uncharted.terarium.hmiserver.service.data.ProvenanceService;

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
	final private ModelConfigurationService modelConfigurationService;
	final private ProvenanceService provenanceService;

	final private String MODEL_CARD_SCRIPT = "gollm:model_card";
	final private String CONFIGURE_MODEL_SCRIPT = "gollm:configure_model";
	final private String DATASET_CONFIGURE_SCRIPT = "gollm:dataset_configure";

	@Data
	private static class ModelCardInput {
		@JsonProperty("research_paper")
		String researchPaper;
	}

	@Data
	private static class ModelCardResponse {
		JsonNode response;
	}

	@Data
	private static class ModelCardProperties {
		UUID documentId;
	}

	@Data
	private static class ConfigureModelInput {
		@JsonProperty("research_paper")
		String researchPaper;
		@JsonProperty("amr")
		Model amr;
	}

	@Data
	private static class ConfigureModelResponse {
		JsonNode response;
	}

	@Data
	private static class ConfigureModelProperties {
		UUID documentId;
		UUID modelId;
	}

	@Data
	private static class ConfigFromDatasetInput {
		@JsonProperty("datasets")
		List<String> datasets;
		@JsonProperty("amr")
		Model amr;
	}

	@Data
	private static class ConfigFromDatasetResponse {
		JsonNode response;
	}

	@Data
	private static class ConfigFromDatasetProperties {
		List<UUID> datasetIds;
		UUID modelId;
	}

	@PostConstruct
	void init() {
		taskService.addResponseHandler(MODEL_CARD_SCRIPT, getModelCardResponseHandler());
		taskService.addResponseHandler(CONFIGURE_MODEL_SCRIPT, configureModelResponseHandler());
		taskService.addResponseHandler(DATASET_CONFIGURE_SCRIPT, configureFromDatasetHandler());
	}

	private TaskResponseHandler getModelCardResponseHandler() {
		final TaskResponseHandler handler = new TaskResponseHandler();
		handler.onSuccess((TaskResponse resp) -> {
			try {
				final ModelCardProperties props = resp.getAdditionalProperties(ModelCardProperties.class);
				log.info("Writing model card to database for document {}", props.getDocumentId());
				final DocumentAsset document = documentAssetService.getAsset(props.getDocumentId())
						.orElseThrow();
				final ModelCardResponse card = objectMapper.readValue(resp.getOutput(), ModelCardResponse.class);
				if (document.getMetadata() == null) {
					document.setMetadata(new java.util.HashMap<>());
				}
				document.getMetadata().put("gollmCard", card.response);

				documentAssetService.updateAsset(document);
			} catch (final Exception e) {
				log.error("Failed to write model card to database", e);
			}
		});

		handler.onRunning((TaskResponse resp) -> {
			log.info(resp.toString());
		});
		return handler;
	}

	private TaskResponseHandler configureModelResponseHandler() {
		final TaskResponseHandler handler = new TaskResponseHandler();
		handler.onSuccess((TaskResponse resp) -> {
			try {
				final ConfigureModelProperties props = resp.getAdditionalProperties(ConfigureModelProperties.class);
				final Model model = modelService.getAsset(props.getModelId())
						.orElseThrow();
				final ConfigureModelResponse configurations = objectMapper.readValue(resp.getOutput(),
						ConfigureModelResponse.class);

				// For each configuration, create a new model configuration with parameters set
				configurations.response.get("conditions").forEach((condition) -> {
					// Map the parameters values to the model
					final Model modelCopy = new Model(model);
					final List<ModelParameter> modelParameters = modelCopy.getSemantics().getOde().getParameters();
					modelParameters.forEach((parameter) -> {
						JsonNode conditionParameters = condition.get("parameters");
						conditionParameters.forEach((conditionParameter) -> {
							if (parameter.getId().equals(conditionParameter.get("id").asText())) {
								parameter.setValue(conditionParameter.get("value").doubleValue());
							}
						});
					});

					// Create the new configuration
					final ModelConfiguration configuration = new ModelConfiguration();
					configuration.setModelId(model.getId());
					configuration.setName(condition.get("name").asText());
					configuration.setDescription(condition.get("description").asText());
					configuration.setConfiguration(modelCopy);

					try {
						final ModelConfiguration newConfig = modelConfigurationService.createAsset(configuration);
						// add provenance
						provenanceService.createProvenance(new Provenance()
								.setLeft(newConfig.getId())
								.setLeftType(ProvenanceType.MODEL_CONFIGURATION)
								.setRight(props.documentId)
								.setRightType(ProvenanceType.DOCUMENT)
								.setRelationType(ProvenanceRelationType.EXTRACTED_FROM));
					} catch (IOException e) {
						log.error("Failed to set model configuration", e);
					}
				});

			} catch (final Exception e) {
				log.error("Failed to configure model", e);
			}
			log.info("Model configured successfully");
		});

		return handler;
	}

	private TaskResponseHandler configureFromDatasetHandler() {
		final TaskResponseHandler handler = new TaskResponseHandler();
		handler.onSuccess((TaskResponse resp) -> {
			try {
				final ConfigFromDatasetProperties props = resp
						.getAdditionalProperties(ConfigFromDatasetProperties.class);

				final Model model = modelService.getAsset(props.getModelId())
						.orElseThrow();
				final ConfigureModelResponse configurations = objectMapper.readValue(resp.getOutput(),
						ConfigureModelResponse.class);

				// For each configuration, create a new model configuration with parameters set
				configurations.response.get("conditions").forEach((condition) -> {
					// Map the parameters values to the model
					final Model modelCopy = new Model(model);
					final List<ModelParameter> modelParameters = modelCopy.getSemantics().getOde().getParameters();
					modelParameters.forEach((parameter) -> {
						JsonNode conditionParameters = condition.get("parameters");
						conditionParameters.forEach((conditionParameter) -> {
							if (parameter.getId().equals(conditionParameter.get("id").asText())) {
								parameter.setValue(conditionParameter.get("value").doubleValue());
							}
						});
					});

					// Create the new configuration
					final ModelConfiguration configuration = new ModelConfiguration();
					configuration.setModelId(model.getId());
					configuration.setName(condition.get("name").asText());
					configuration.setDescription(condition.get("description").asText());
					configuration.setConfiguration(modelCopy);

					try {
						for (UUID datasetId : props.datasetIds) {
							final ModelConfiguration newConfig = modelConfigurationService.createAsset(configuration);
							// add provenance
							provenanceService.createProvenance(new Provenance()
									.setLeft(newConfig.getId())
									.setLeftType(ProvenanceType.MODEL_CONFIGURATION)
									.setRight(datasetId)
									.setRightType(ProvenanceType.DATASET)
									.setRelationType(ProvenanceRelationType.EXTRACTED_FROM));
						}

					} catch (IOException e) {
						log.error("Failed to set model configuration", e);
					}
				});

			} catch (final Exception e) {
				log.error("Failed to configure model", e);
			}
			log.info("Model configured successfully");
		});

		return handler;
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

			// check for input length
			if (document.get().getText().length() > 600000) {
				log.warn("Document {} text too long for GoLLM model card task", documentId);
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document text is too long");
			}

			final ModelCardInput input = new ModelCardInput();
			input.setResearchPaper(document.get().getText());

			// Create the task
			final TaskRequest req = new TaskRequest();
			req.setId(java.util.UUID.randomUUID());
			req.setScript(MODEL_CARD_SCRIPT);
			req.setInput(objectMapper.writeValueAsBytes(input));

			final ModelCardProperties props = new ModelCardProperties();
			props.setDocumentId(documentId);
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
			@RequestParam(name = "document-ids", required = true) final List<UUID> datasetIds) {

		try {

			// Grab the datasets
			List<String> datasets = new ArrayList<>();
			for (UUID datasetId : datasetIds) {
				final Optional<Dataset> dataset = datasetService.getAsset(datasetId);
				if (dataset.isEmpty()) {
					throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset not found");
				}

				// make sure there is text in the document
				if (dataset.get().getFileNames() == null || dataset.get().getFileNames().isEmpty()) {
					log.warn("Dataset {} has no source files to send", datasetId);
					throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset has no filenames");
				}

				for (String filename : dataset.get().getFileNames()) {
					try {
						Optional<String> datasetText = datasetService.fetchFileAsString(datasetId, filename);
						if (dataset.isPresent()) {
							datasets.add(datasetText.get());
						}
					} catch (Exception e) {
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

			final ConfigFromDatasetInput input = new ConfigFromDatasetInput();
			input.setDatasets(datasets);
			input.setAmr(model.get());

			// Create the task
			final TaskRequest req = new TaskRequest();
			req.setId(java.util.UUID.randomUUID());
			req.setScript(CONFIGURE_MODEL_SCRIPT);
			req.setInput(objectMapper.writeValueAsBytes(input));

			final ConfigFromDatasetProperties props = new ConfigFromDatasetProperties();
			props.setDatasetIds(datasetIds);
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
