package software.uncharted.terarium.hmiserver.controller.gollm;

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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import software.uncharted.terarium.hmiserver.annotations.IgnoreRequestLogging;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.models.task.TaskStatus;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.TaskResponseHandler;
import software.uncharted.terarium.hmiserver.service.TaskService;
import software.uncharted.terarium.hmiserver.service.data.DocumentAssetService;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@RequestMapping("/gollm")
@RestController
@Slf4j
@RequiredArgsConstructor
public class GoLLMController {

	final private ObjectMapper objectMapper;
	final private TaskService taskService;
	final private DocumentAssetService documentAssetService;

	final private String MODEL_CARD_SCRIPT = "gollm:model_card";

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

    @PostConstruct
	void init() {
		taskService.addResponseHandler(MODEL_CARD_SCRIPT, getModelCardResponseHandler());
	}

	private TaskResponseHandler getModelCardResponseHandler() {
		final TaskResponseHandler handler = new TaskResponseHandler();
		handler.onSuccess((TaskResponse resp) -> {
			try {
				final String serializedString = objectMapper.writeValueAsString(resp.getAdditionalProperties());
				final ModelCardProperties props = objectMapper.readValue(serializedString, ModelCardProperties.class);
				log.info("Writing model card to database for document {}", props.getDocumentId());
				final DocumentAsset document = documentAssetService.getAsset(props.getDocumentId())
						.orElseThrow();
				final ModelCardResponse card = objectMapper.readValue(resp.getOutput(), ModelCardResponse.class);
				if (document.getMetadata() == null){
					document.setMetadata(new java.util.HashMap<>());
				}
				document.getMetadata().put("gollmCard", card.response);
				
				documentAssetService.updateAsset(document);
			} catch (final IOException e) {
				log.error("Failed to write model card to database", e);
			}
		});

		handler.onRunning((TaskResponse resp) -> {
			log.info(resp.toString());
		});
		return handler;
	}

	@PostMapping("/model-card")
	@Secured(Roles.USER)
	@Operation(summary = "Dispatch a `GoLLM Model Card task")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Dispatched successfully", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TaskResponse.class))),
			@ApiResponse(responseCode = "404", description = "The provided model or document arguments are not found", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue dispatching the request", content = @Content)
	})
	public ResponseEntity<TaskResponse> createModelCardTask(
			@RequestParam(name = "document-id", required = true) final UUID documentId) {

		try {
			// Grab the document
			final Optional<DocumentAsset> document = documentAssetService.getAsset(documentId);
			if (document.isEmpty()) {
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
			taskService.sendTaskRequest(req);

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
