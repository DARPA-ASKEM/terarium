package software.uncharted.terarium.hmiserver.controller.gollm;

import java.io.IOException;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
				model.setModelCard(new String(resp.getOutput()));
				modelService.updateModel(model);
			} catch (IOException e) {
				log.error("Failed to write model card to database", e);
			}
		});
		return handler;
	}

	@PostMapping("/model_card")
	@Secured(Roles.USER)
	public ResponseEntity<TaskResponse> createModelCardTask(
			@RequestParam(name = "document-id", required = true) UUID documentId,
			@RequestParam(name = "model-id", required = true) UUID modelId)
			throws JsonProcessingException, IOException {

		// Ensure the model is valid
		modelService.getModel(modelId).orElseThrow();

		// Grab the document
		DocumentAsset document = documentAssetService.getDocumentAsset(documentId)
				.orElseThrow();

		ModelCardInput input = new ModelCardInput();
		input.setResearchPaper(document.getText());

		// Create the task
		TaskRequest req = new TaskRequest();
		req.setId(java.util.UUID.randomUUID());
		req.setScript(MODEL_CARD_SCRIPT);
		req.setInput(objectMapper.writeValueAsBytes(input));

		ModelCardProperties props = new ModelCardProperties();
		props.setModelId(modelId);
		props.setDocumentId(documentId);
		req.setAdditionalProperties(props);

		try {
			// send the request
			taskService.sendTaskRequest(req);
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}

		TaskResponse resp = new TaskResponse();
		resp.setId(req.getId());
		resp.setStatus(TaskStatus.QUEUED);
		return ResponseEntity.ok().body(resp);
	}

	@PutMapping("/{task-id}")
	public ResponseEntity<Void> cancelTask(@PathVariable("task-id") final UUID taskId) {
		taskService.cancelTask(taskId);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{task-id}")
	@IgnoreRequestLogging
	public SseEmitter subscribe(@PathVariable("task-id") final UUID taskId) {
		return taskService.subscribe(taskId);
	}
}
