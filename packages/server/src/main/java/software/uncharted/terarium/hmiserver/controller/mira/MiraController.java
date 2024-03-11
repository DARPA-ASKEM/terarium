package software.uncharted.terarium.hmiserver.controller.mira;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import software.uncharted.terarium.hmiserver.annotations.IgnoreRequestLogging;
import software.uncharted.terarium.hmiserver.models.dataservice.Artifact;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.mira.Curies;
import software.uncharted.terarium.hmiserver.models.mira.DKG;
import software.uncharted.terarium.hmiserver.models.mira.EntitySimilarityResult;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest.TaskType;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.proxies.mira.MIRAProxy;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.ArtifactService;
import software.uncharted.terarium.hmiserver.service.tasks.MdlToStockflowResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.SbmlToPetrinetResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.StellaToStockflowResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.AMRToMMTResponseHandler;
import software.uncharted.terarium.hmiserver.service.tasks.TaskService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequestMapping("/mira")
@RestController
@Slf4j
@RequiredArgsConstructor
public class MiraController {

	final private ObjectMapper objectMapper;
	final private ArtifactService artifactService;
	final private TaskService taskService;

	@Autowired
	MIRAProxy proxy;

	@Data
	static public class ModelConversionRequest {
		public UUID artifactId;
	}

	@Data
	static public class ModelConversionResponse {
		public Model response;
	}

	private static boolean endsWith(final String filename, final List<String> suffixes) {
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

	@PostMapping("/amr-to-mmt")
	@Secured(Roles.USER)
	@Operation(summary = "convert AMR to MIRA model template")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Dispatched successfully", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TaskResponse.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue dispatching the request", content = @Content)
	})
	public ResponseEntity<JsonNode> convertAMRtoMMT(@RequestBody final String modelStr) {
		try {
			final TaskRequest req = new TaskRequest();
			req.setType(TaskType.MIRA);
			req.setInput(modelStr.getBytes());
			req.setScript(AMRToMMTResponseHandler.NAME);

			// send the request
			final TaskResponse resp = taskService.runTaskSync(req);
			final JsonNode model = objectMapper.readValue(resp.getOutput(), JsonNode.class);
			return ResponseEntity.ok().body(model);
		} catch (final Exception e) {
			final String error = "Unable to dispatch task request";
			log.error("Unable to dispatch task request {}: {}", error, e.getMessage());
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}



	@PostMapping("/convert-and-create-model")
	@Secured(Roles.USER)
	@Operation(summary = "Dispatch a MIRA conversion task")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Dispatched successfully", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TaskResponse.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue dispatching the request", content = @Content)
	})
	public ResponseEntity<Model> convertAndCreateModel(
			@RequestBody final ModelConversionRequest conversionRequest) {

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
			req.setType(TaskType.MIRA);
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
			final TaskResponse resp = taskService.runTaskSync(req);
			final Model model = objectMapper.readValue(resp.getOutput(), Model.class);
			return ResponseEntity.ok().body(model);

		} catch (final Exception e) {
			final String error = "Unable to dispatch task request";
			log.error("Unable to dispatch task request {}: {}", error, e.getMessage());
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

	@GetMapping("/currie/{curies}")
	@Secured(Roles.USER)
	public ResponseEntity<List<DKG>> searchConcept(
		@PathVariable("curies") final String curies) {
		try {
			final ResponseEntity<List<DKG>> response = proxy.getEntities(curies);
			if (response.getStatusCode().is2xxSuccessful()) {
				return ResponseEntity.ok(response.getBody());
			}
			return ResponseEntity.internalServerError().build();
		} catch (final FeignException.NotFound e) { // Handle 404 errors
			log.info("Could not find resource in the DKG", e);
			return ResponseEntity.noContent().build();
		} catch (final Exception e) {
			log.error("Unable to fetch DKG", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@GetMapping("/search")
	@Secured(Roles.USER)
	public ResponseEntity<List<DKG>> search(
		@RequestParam("q") final String q,
		@RequestParam(required = false, name = "limit", defaultValue = "10") final Integer limit,
		@RequestParam(required = false, name = "offset", defaultValue = "0") final Integer offset) {
		try {
			final ResponseEntity<List<DKG>> response = proxy.search(q, limit, offset);
			if (response.getStatusCode().is2xxSuccessful()) {
				return ResponseEntity.ok(response.getBody());
			}
			return ResponseEntity.internalServerError().build();
		} catch (final FeignException.NotFound e) { // Handle 404 errors
			log.info("Could not find resource in the DKG", e);
			return ResponseEntity.notFound().build();
		} catch (final Exception e) {
			log.error("Unable to fetch DKG", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	// This rebuilds the semantics ODE via MIRA
	// 1. Send AMR to MIRA => MIRANet
	// 2. Send MIRANet to MIRA to convert back to AMR Petrinet
	// 3. Send AMR back
	@PostMapping("/reconstruct-ode-semantics")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> reconstructODESemantics(
		final Object amr) {
		return ResponseEntity.ok(proxy.reconstructODESemantics(amr).getBody());

	}

	@PostMapping("/entity-similarity")
	@Secured(Roles.USER)
	public ResponseEntity<List<EntitySimilarityResult>> entitySimilarity(
		@RequestBody final Curies obj) {
		return ResponseEntity.ok(proxy.entitySimilarity(obj).getBody());
	}
}
