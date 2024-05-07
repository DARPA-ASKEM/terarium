package software.uncharted.terarium.hmiserver.controller.simulationservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.controller.SnakeCaseController;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.ProgressState;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.Simulation;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.SimulationEngine;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.SimulationType;
import software.uncharted.terarium.hmiserver.models.simulationservice.CalibrationRequestCiemss;
import software.uncharted.terarium.hmiserver.models.simulationservice.CalibrationRequestJulia;
import software.uncharted.terarium.hmiserver.models.simulationservice.EnsembleCalibrationCiemssRequest;
import software.uncharted.terarium.hmiserver.models.simulationservice.EnsembleSimulationCiemssRequest;
import software.uncharted.terarium.hmiserver.models.simulationservice.JobResponse;
import software.uncharted.terarium.hmiserver.models.simulationservice.OptimizeRequestCiemss;
import software.uncharted.terarium.hmiserver.models.simulationservice.SimulationRequest;
import software.uncharted.terarium.hmiserver.models.simulationservice.parts.Intervention;
import software.uncharted.terarium.hmiserver.proxies.simulationservice.SimulationCiemssServiceProxy;
import software.uncharted.terarium.hmiserver.proxies.simulationservice.SimulationServiceProxy;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.data.ModelConfigurationService;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;
import software.uncharted.terarium.hmiserver.service.data.SimulationService;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@RequestMapping("/simulation-request")
@RestController
@Slf4j
@RequiredArgsConstructor
// TODO: Once we've moved this off of TDS remove the SnakeCaseController
// interface and import.
public class SimulationRequestController implements SnakeCaseController {

	private final CurrentUserService currentUserService;

	private final SimulationServiceProxy simulationServiceProxy;

	private final SimulationCiemssServiceProxy simulationCiemssServiceProxy;

	private final ProjectService projectService;
	private final SimulationService simulationService;

	private final ModelService modelService;
	private final ModelConfigurationService modelConfigService;

	private final ObjectMapper objectMapper;

	@Value("${terarium.sciml-queue}")
	private String SCIML_QUEUE;

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<Simulation> getSimulation(@PathVariable("id") final UUID id, @RequestParam("project-id") final UUID projectId) {
		Schema.Permission permission = projectService.checkPermissionCanRead(currentUserService.get().getId(), projectId);

		try {
			final Optional<Simulation> sim = simulationService.getAsset(id, permission);
			if (sim.isEmpty()) {
				return ResponseEntity.noContent().build();
			}
			return ResponseEntity.ok(sim.get());
		} catch (final Exception e) {
			final String error = String.format("Failed to get result of simulation %s", id);
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@PostMapping("/forecast")
	@Secured(Roles.USER)
	public ResponseEntity<Simulation> makeForecastRun(@RequestBody final SimulationRequest request) {
		final Schema.Permission permission = projectService.checkPermissionCanWrite(currentUserService.get().getId(), request.getProjectId());

		final JobResponse res = simulationServiceProxy
				.makeForecastRun(convertObjectToSnakeCaseJsonNode(request))
				.getBody();

		final Simulation sim = new Simulation();
		sim.setId(UUID.fromString(res.getSimulationId()));
		sim.setType(SimulationType.SIMULATION);

		// FIXME: engine is set twice, talk to TDS
		request.setEngine(SimulationEngine.SCIML.toString());

		sim.setExecutionPayload(objectMapper.convertValue(request, JsonNode.class));
		sim.setStatus(ProgressState.QUEUED);

		// FIXME: These fiels are arguable unnecessary
		final Optional<Project> project = projectService.getProject(request.getProjectId());
		if (project.isPresent()) {
			sim.setProjectId(project.get().getId());
			sim.setUserId(project.get().getUserId());
		}
		sim.setEngine(SimulationEngine.SCIML);

		try {
			final Optional<Simulation> updated = simulationService.updateAsset(sim, permission);
			if (updated.isEmpty()) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.ok(updated.get());
		} catch (final Exception e) {
			final String error = "Failed to create simulation";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@PostMapping("ciemss/forecast")
	@Secured(Roles.USER)
	public ResponseEntity<Simulation> makeForecastRunCiemss(@RequestBody final SimulationRequest request) {
		final Schema.Permission permission = projectService.checkPermissionCanWrite(currentUserService.get().getId(), request.getProjectId());

		// Get model config's interventions and append them to requests:
		try {
			final Optional<ModelConfiguration> modelConfiguration =
					modelConfigService.getAsset(request.getModelConfigId(), permission);
			if (modelConfiguration.isEmpty()) {
				return ResponseEntity.notFound().build();
			}
			final List<Intervention> modelInterventions =
					modelConfiguration.get().getInterventions();
			if (modelInterventions != null) {
				List<Intervention> allInterventions = request.getInterventions();
				if (allInterventions == null) {
					allInterventions = new ArrayList<Intervention>();
				}
				allInterventions.addAll(modelInterventions);
				request.setInterventions(allInterventions);
			}
		} catch (IOException e) {
			String error = "Unable to find model configuration";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}

		final JobResponse res = simulationCiemssServiceProxy
				.makeForecastRun(convertObjectToSnakeCaseJsonNode(request))
				.getBody();

		final Simulation sim = new Simulation();
		sim.setId(UUID.fromString(res.getSimulationId()));
		sim.setType(SimulationType.SIMULATION);

		// FIXME: engine is set twice, talk to TDS
		request.setEngine(SimulationEngine.CIEMSS.toString());

		sim.setExecutionPayload(objectMapper.convertValue(request, JsonNode.class));
		sim.setStatus(ProgressState.QUEUED);

		final Optional<Project> project = projectService.getProject(request.getProjectId());
		if (project.isPresent()) {
			sim.setProjectId(project.get().getId());
			sim.setUserId(project.get().getUserId());
		}

		sim.setEngine(SimulationEngine.CIEMSS);

		try {
			final Optional<Simulation> updated = simulationService.updateAsset(sim, permission);
			if (updated.isEmpty()) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.ok(updated.get());
		} catch (final Exception e) {
			final String error = "Failed to create simulation";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@PostMapping("/calibrate")
	@Secured(Roles.USER)
	public ResponseEntity<JobResponse> makeCalibrateJob(@RequestBody final CalibrationRequestJulia request) {
		return ResponseEntity.ok(simulationServiceProxy
				.makeCalibrateJob(SCIML_QUEUE, convertObjectToSnakeCaseJsonNode(request))
				.getBody());
	}

	@PostMapping("ciemss/calibrate")
	@Secured(Roles.USER)
	public ResponseEntity<JobResponse> makeCalibrateJobCiemss(@RequestBody final CalibrationRequestCiemss request) {
		return ResponseEntity.ok(simulationCiemssServiceProxy
				.makeCalibrateJob(convertObjectToSnakeCaseJsonNode(request))
				.getBody());
	}

	@PostMapping("ciemss/optimize")
	@Secured(Roles.USER)
	public ResponseEntity<JobResponse> makeOptimizeJobCiemss(@RequestBody final OptimizeRequestCiemss request) {
		return ResponseEntity.ok(simulationCiemssServiceProxy
				.makeOptimizeJob(convertObjectToSnakeCaseJsonNode(request))
				.getBody());
	}

	@PostMapping("ciemss/ensemble-simulate")
	@Secured(Roles.USER)
	public ResponseEntity<JobResponse> makeEnsembleSimulateCiemssJob(
			@RequestBody final EnsembleSimulationCiemssRequest request) {
		return ResponseEntity.ok(simulationCiemssServiceProxy
				.makeEnsembleSimulateCiemssJob(convertObjectToSnakeCaseJsonNode(request))
				.getBody());
	}

	@PostMapping("ciemss/ensemble-calibrate")
	@Secured(Roles.USER)
	public ResponseEntity<JobResponse> makeEnsembleCalibrateCiemssJob(
			@RequestBody final EnsembleCalibrationCiemssRequest request) {
		return ResponseEntity.ok(simulationCiemssServiceProxy
				.makeEnsembleCalibrateCiemssJob(convertObjectToSnakeCaseJsonNode(request))
				.getBody());
	}

	@GetMapping("ciemss/cancel/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<JobResponse> cancelCiemssJob(@PathVariable("id") final UUID id) {
		try {
			return ResponseEntity.ok(simulationCiemssServiceProxy.cancelJob(id).getBody());
		} catch (final FeignException e) {
			final String error = "Unable to cancel ciemss job " + id.toString();
			final int status = e.status() >= 400 ? e.status() : 500;
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.valueOf(status), error);
		}
	}
}
