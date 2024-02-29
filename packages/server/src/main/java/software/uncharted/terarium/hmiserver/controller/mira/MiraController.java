package software.uncharted.terarium.hmiserver.controller.mira;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.dataservice.Artifact;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.models.task.TaskStatus;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.ArtifactService;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
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
	final private ModelService modelService;
	final private TaskService taskService;

	static final public String STELLA_TO_STOCKFLOW = "mira_task:stella_to_stockflow";
	static final public String MDL_TO_STOCKFLOW = "mira_task:mdl_to_stockflow";
	static final public String SBML_TO_PETRINET = "mira_task:sbml_to_petrinet";
	static final private long REQUEST_TIMEOUT_SECONDS = 30;

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

			final Optional<String> fileContents = artifactService.fetchFileAsString(conversionRequest.artifactId, filename);
			if (fileContents.isEmpty()) {
				throw new ResponseStatusException(
						org.springframework.http.HttpStatus.BAD_REQUEST,
						"Unable to fetch file contents");
			}

			final TaskRequest req = new TaskRequest();
			req.setInput(fileContents.get().getBytes());

			if (endsWith(filename, List.of(".mdl"))) {
				req.setScript(MDL_TO_STOCKFLOW);
			} else if (endsWith(filename, List.of(".xmile", ".itmx", ".stmx"))) {
				req.setScript(STELLA_TO_STOCKFLOW);
			} else if (endsWith(filename, List.of(".sbml", ".xml"))) {
				req.setScript(SBML_TO_PETRINET);
			} else {
				throw new ResponseStatusException(
						org.springframework.http.HttpStatus.BAD_REQUEST,
						"Unknown model type");
			}

			final List<TaskResponse> responses = taskService.runTaskBlocking(req, TaskService.TaskType.MIRA,
					REQUEST_TIMEOUT_SECONDS);

			final TaskResponse resp = responses.get(responses.size() - 1);

			if (resp.getStatus() != TaskStatus.SUCCESS) {
				throw new ResponseStatusException(
						org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
						"Unable to generate vectors for knn search");
			}

			final byte[] outputBytes = resp.getOutput();
			final JsonNode output = objectMapper.readTree(outputBytes);

			final ModelConversionResponse modelResp = objectMapper.convertValue(output, ModelConversionResponse.class);

			return ResponseEntity.status(HttpStatus.CREATED).body(modelService.createAsset(modelResp.getResponse()));

		} catch (final Exception e) {
			final String error = "Unable to dispatch task request";
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}
}
