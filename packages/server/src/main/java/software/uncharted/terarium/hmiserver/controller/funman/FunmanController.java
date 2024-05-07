package software.uncharted.terarium.hmiserver.controller.funman;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.JsonNodeProjectIdRequestBody;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.ProgressState;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.Simulation;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.SimulationType;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest.TaskType;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;
import software.uncharted.terarium.hmiserver.service.data.SimulationService;
import software.uncharted.terarium.hmiserver.service.tasks.TaskService;
import software.uncharted.terarium.hmiserver.service.tasks.TaskService.TaskMode;
import software.uncharted.terarium.hmiserver.service.tasks.ValidateModelConfigHandler;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

import java.util.UUID;

@RestController
@RequestMapping("/funman/queries")
@RequiredArgsConstructor
@Slf4j
public class FunmanController {

	private final ObjectMapper objectMapper;
	private final TaskService taskService;
	private final CurrentUserService currentUserService;
	private final ProjectService projectService;

	private final ValidateModelConfigHandler validateModelConfigHandler;
	private final SimulationService simulationService;

	@PostConstruct
	void init() {
		taskService.addResponseHandler(validateModelConfigHandler);
	}

	@PostMapping
	@Secured(Roles.USER)
	@Operation(summary = "Dispatch a model configuration validation task")
	@ApiResponses(
			value = {
				@ApiResponse(
						responseCode = "200",
						description = "Dispatched successfully",
						content =
								@Content(
										mediaType = "application/json",
										schema =
												@io.swagger.v3.oas.annotations.media.Schema(
														implementation = Simulation.class))),
				@ApiResponse(responseCode = "400", description = "Invalid input or bad request", content = @Content),
				@ApiResponse(
						responseCode = "500",
						description = "There was an issue dispatching the request",
						content = @Content)
			})
	public ResponseEntity<Simulation> createValidationRequest(@RequestBody final JsonNodeProjectIdRequestBody request) {
		Schema.Permission permission = projectService.checkPermissionCanWrite(currentUserService.get().getId(), request.getProjectId());

		try {
			JsonNode input = request.getJsonNode();
			final TaskRequest taskRequest = new TaskRequest();
			taskRequest.setType(TaskType.FUNMAN);
			taskRequest.setScript(ValidateModelConfigHandler.NAME);
			taskRequest.setUserId(currentUserService.get().getId());
			taskRequest.setInput(objectMapper.writeValueAsBytes(input));

			final Simulation sim = new Simulation();
			sim.setType(SimulationType.VALIDATION);
			sim.setStatus(ProgressState.RUNNING);
			sim.setDescription("funman model configuration validation");
			sim.setExecutionPayload(objectMapper.convertValue(input, JsonNode.class));

			// Create new simulatin object to proxy the funman validation process
			Simulation newSimulation = simulationService.createAsset(sim, permission);

			final ValidateModelConfigHandler.Properties props = new ValidateModelConfigHandler.Properties();
			props.setSimulationId(newSimulation.getId());
			taskRequest.setAdditionalProperties(props);
			taskService.runTask(TaskMode.ASYNC, taskRequest);

			return ResponseEntity.ok(newSimulation);
		} catch (final ResponseStatusException e) {
			e.printStackTrace();
			throw e;
		} catch (final Exception e) {
			e.printStackTrace();
			final String error = "Unable to dispatch task request";
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}
}
