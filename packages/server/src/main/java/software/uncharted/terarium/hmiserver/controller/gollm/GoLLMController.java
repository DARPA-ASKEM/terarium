package software.uncharted.terarium.hmiserver.controller.gollm;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

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
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.models.task.TaskStatus;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.TaskResponseHandler;
import software.uncharted.terarium.hmiserver.service.TaskService;
import software.uncharted.terarium.hmiserver.service.data.DocumentAssetService;
import software.uncharted.terarium.hmiserver.service.data.ModelService;

@RequestMapping("/gollm")
@RestController
@Slf4j
@RequiredArgsConstructor
public class GoLLMController {

	final private ObjectMapper objectMapper;
	final private TaskService taskService;
	final private DocumentAssetService documentAssetService;
	final private ModelService modelService;

	final private String MODEL_CARD_SCRIPT = "gollm:model_card";

	@Data
	private static class ModelCardInput {
		@JsonProperty("research_paper")
		String researchPaper;
	};

	@Data
	private static class ModelCardProperties {
		UUID modelId;
		UUID documentId;
	};

	@PostConstruct
	void init() {
		taskService.addResponseHandler(MODEL_CARD_SCRIPT, getModelCardResponseHandler());
	}

	private TaskResponseHandler getModelCardResponseHandler() {
		TaskResponseHandler handler = new TaskResponseHandler();
		handler.onSuccess((TaskResponse resp) -> {
			ModelCardProperties props = resp.getAdditionalProperties(ModelCardProperties.class);
			log.info("Writing model card to database for model {}", props.getModelId());
			try {
				Model model = modelService.getModel(props.getModelId())
						.orElseThrow();
				JsonNode card = objectMapper.readTree(resp.getOutput());
				model.getMetadata().setGollmCard(card);
				modelService.updateModel(model);
			} catch (IOException e) {
				log.error("Failed to write model card to database", e);
			}
		});

		handler.onRunning((TaskResponse resp) -> {
			log.info(resp.toString());
		});
		return handler;
	}

	@PostMapping("/model_card")
	@Secured(Roles.USER)
	@Operation(summary = "Dispatch a `GoLLM Model Card task")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Dispatched successfully", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TaskResponse.class))),
			@ApiResponse(responseCode = "404", description = "The provided model or document arguments are not found", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue dispatching the request", content = @Content)
	})
	public ResponseEntity<TaskResponse> createModelCardTask(
			@RequestParam(name = "document-id", required = true) UUID documentId,
			@RequestParam(name = "model-id", required = true) UUID modelId) {

		try {
			// Ensure the model is valid
			Optional<Model> model = modelService.getModel(modelId);
			if (!model.isPresent()) {
				return ResponseEntity.notFound().build();
			}

			// Grab the document
			Optional<DocumentAsset> document = documentAssetService.getDocumentAsset(documentId);
			if (!document.isPresent()) {
				return ResponseEntity.notFound().build();
			}

			// make sure there is text in the document
			if (document.get().getText() == null || document.get().getText().isEmpty()) {
				log.warn("Document {} has no text to send", documentId);
				return ResponseEntity.badRequest().build();
			}

			// check for input length
			if (document.get().getText().length() > 600000) {
				log.warn("Document {} text too long for GoLLM model card task", documentId);
				return ResponseEntity.badRequest().build();
			}

			ModelCardInput input = new ModelCardInput();
			input.setResearchPaper(document.get().getText());

			// Create the task
			TaskRequest req = new TaskRequest();
			req.setId(java.util.UUID.randomUUID());
			req.setScript(MODEL_CARD_SCRIPT);
			req.setInput(objectMapper.writeValueAsBytes(input));

			ModelCardProperties props = new ModelCardProperties();
			props.setModelId(modelId);
			props.setDocumentId(documentId);
			req.setAdditionalProperties(props);

			// send the request
			taskService.sendTaskRequest(req);

			TaskResponse resp = req.createResponse(TaskStatus.QUEUED);
			return ResponseEntity.ok().body(resp);

		} catch (Exception e) {
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
