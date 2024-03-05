package software.uncharted.terarium.hmiserver.controller.mira;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import software.uncharted.terarium.hmiserver.models.dataservice.Artifact;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.ArtifactService;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
import software.uncharted.terarium.hmiserver.service.tasks.MdlToStockflowResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.SbmlToPetrinetResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.StellaToStockflowResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.TaskService;
import software.uncharted.terarium.hmiserver.service.tasks.TaskService.TaskMode;

@RequestMapping("/mira")
@RestController
@Slf4j
@RequiredArgsConstructor
public class MiraController {

	final private ArtifactService artifactService;
	final private ObjectMapper objectMapper;
	final private TaskService taskService;
	final private ModelService modelService;

	@Data
	static public class ModelConversionRequest {
		public UUID artifactId;
	};

	@Data
	static public class ModelConversionResponse {
		public Model response;
	};

	private boolean endsWith(final String filename, final List<String> suffixes) {
		for (final String suffix : suffixes) {
			if (filename.endsWith(suffix)) {
				return true;
			}
		}
		return false;
	}

	final private StellaToStockflowResponseHandler stellaToStockflowResponseHandler;
	final private MdlToStockflowResponseHandler mdlToStockflowResponseHandler;
	final private SbmlToPetrinetResponseHandler sbmlToPetrinetResponseHandler;

	@PostConstruct
	void init() {
		taskService.addResponseHandler(stellaToStockflowResponseHandler);
		taskService.addResponseHandler(mdlToStockflowResponseHandler);
		taskService.addResponseHandler(sbmlToPetrinetResponseHandler);
	}

	@PostMapping("/convert-and-create-model")
	@Secured(Roles.USER)
	@Operation(summary = "Dispatch a Mira conversion task")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Dispatched successfully", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TaskResponse.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue dispatching the request", content = @Content)
	})
	public ResponseEntity<TaskResponse> convertAndCreateModel(
			@RequestBody final ModelConversionRequest conversionRequest,
			@RequestParam(name = "mode", required = false, defaultValue = "async") final TaskMode mode) {

		try {

			final Optional<Artifact> artifact = artifactService.getAsset(conversionRequest.artifactId);
			if (artifact.isEmpty()) {
				throw new ResponseStatusException(
						org.springframework.http.HttpStatus.BAD_REQUEST,
						"Artifact not found");
			}

			if (artifact.get().getFileNames().isEmpty()) {
				throw new ResponseStatusException(
						org.springframework.http.HttpStatus.BAD_REQUEST,
						"Artifact has no files");
			}

			final String filename = artifact.get().getFileNames().get(0);

			final Optional<String> fileContents = artifactService.fetchFileAsString(conversionRequest.artifactId,
					filename);
			if (fileContents.isEmpty()) {
				throw new ResponseStatusException(
						org.springframework.http.HttpStatus.BAD_REQUEST,
						"Unable to fetch file contents");
			}

			final TaskRequest req = new TaskRequest();
			req.setInput(fileContents.get().getBytes());

			if (endsWith(filename, List.of(".mdl"))) {
				req.setScript(MdlToStockflowResponseHandler.NAME);
			} else if (endsWith(filename, List.of(".xmile", ".itmx", ".stmx"))) {
				req.setScript(StellaToStockflowResponseHandler.NAME);
			} else if (endsWith(filename, List.of(".sbml", ".xml"))) {
				req.setScript(SbmlToPetrinetResponseHandler.NAME);
			} else {
				throw new ResponseStatusException(
						org.springframework.http.HttpStatus.BAD_REQUEST,
						"Unknown model type");
			}

			// send the request
			return ResponseEntity.ok().body(taskService.runTask(mode, req));

		} catch (final Exception e) {
			final String error = "Unable to dispatch task request";
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@PutMapping("/{task-id}")
	@Operation(summary = "Cancel a Mira task")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Dispatched cancellation successfully", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Void.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue dispatching the cancellation", content = @Content)
	})
	public ResponseEntity<Void> cancelTask(@PathVariable("task-id") final UUID taskId) {
		taskService.cancelTask(taskId);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{task-id}")
	@Operation(summary = "Subscribe for updates on a Mira task")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Subscribed successfully", content = @Content(mediaType = "text/event-stream", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TaskResponse.class))),
	})
	@IgnoreRequestLogging
	public SseEmitter subscribe(@PathVariable("task-id") final UUID taskId) {
		return taskService.subscribe(taskId);
	}
}
