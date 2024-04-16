package software.uncharted.terarium.hmiserver.controller.simulationservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
import software.uncharted.terarium.hmiserver.service.data.ModelConfigurationService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;
import software.uncharted.terarium.hmiserver.service.data.SimulationService;

@RequestMapping("/simulation-request")
@RestController
@Slf4j
@RequiredArgsConstructor
// TODO: Once we've moved this off of TDS remove the SnakeCaseController
// interface and import.
public class SimulationRequestController implements SnakeCaseController {

	private final SimulationServiceProxy simulationServiceProxy;

	private final SimulationCiemssServiceProxy simulationCiemssServiceProxy;

	private final ProjectService projectService;
	private final SimulationService simulationService;

	private final ModelConfigurationService modelConfigService;

	@Value("${terarium.sciml-queue}")
	private String SCIML_QUEUE;

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<Simulation> getSimulation(@PathVariable("id") final UUID id) {

		try {
			final Optional<Simulation> sim = simulationService.getSimulation(id);
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
		final JobResponse res = simulationServiceProxy
				.makeForecastRun(convertObjectToSnakeCaseJsonNode(request))
				.getBody();

		final Simulation sim = new Simulation();
		sim.setId(UUID.fromString(res.getSimulationId()));
		sim.setType(SimulationType.SIMULATION);

		// FIXME: engine is set twice, talk to TDS
		request.setEngine(SimulationEngine.SCIML.toString());

		sim.setExecutionPayload(request);
		sim.setStatus(ProgressState.QUEUED);

		// FIXME: These fiels are arguable unnecessary
		final Optional<Project> project = projectService.getProject(request.getProjectId());
		if (project.isPresent()) {
			sim.setProjectId(project.get().getId());
			sim.setUserId(project.get().getUserId());
		}
		sim.setEngine(SimulationEngine.SCIML);

		try {
			final Optional<Simulation> updated = simulationService.updateSimulation(sim);
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

		if (request.getInterventions() == null) {
			request.setInterventions(getInterventionFromId(request.getModelConfigId()));
		}

		final JobResponse res = simulationCiemssServiceProxy
				.makeForecastRun(convertObjectToSnakeCaseJsonNode(request))
				.getBody();

		final Simulation sim = new Simulation();
		sim.setId(UUID.fromString(res.getSimulationId()));
		sim.setType(SimulationType.SIMULATION);

		// FIXME: engine is set twice, talk to TDS
		request.setEngine(SimulationEngine.CIEMSS.toString());

		sim.setExecutionPayload(request);
		sim.setStatus(ProgressState.QUEUED);

		final Optional<Project> project = projectService.getProject(request.getProjectId());
		if (project.isPresent()) {
			sim.setProjectId(project.get().getId());
			sim.setUserId(project.get().getUserId());
		}

		sim.setEngine(SimulationEngine.CIEMSS);

		try {
			final Optional<Simulation> updated = simulationService.updateSimulation(sim);
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
		return ResponseEntity.ok(simulationCiemssServiceProxy.cancelJob(id).getBody());
	}

	// Get modelConfigId
	// Check if it has timeseries in its metadata
	// If it does for each element convert it to type Intervention and add it to
	// this.interventions
	// Schema: http://json-schema.org/draft-07/schema#
	private List<Intervention> getInterventionFromId(final UUID modelConfigId) {
		final List<Intervention> interventionList = new ArrayList<>();
		try {
			final ObjectMapper mapper = new ObjectMapper();
			final Optional<ModelConfiguration> modelConfig = modelConfigService.getAsset(modelConfigId);
			final JsonNode configuration = mapper.convertValue(modelConfig.get().getConfiguration(), JsonNode.class);
			// Parse the values found under the following path:
			// AMR -> configuration -> metadata -> timeseries -> parameter name -> value
			// EG) "timeseries": {
			// "beta": "1:0.05,2:0.04,3:0.01"
			// }
			// Into the following format: "interventions":
			// [{"timestep":1,"name":"beta","value":0.05},
			// {"timestep":2,"name":"beta","value":0.04}, ...]
			// This will later be scrapped after a redesign where our AMR -> configuration
			// -> metadata -> timeseries -> parameter name -> value should be more typed.
			if (configuration.get("metadata").get("timeseries") != null) {
				final JsonNode timeseries =
						mapper.convertValue(configuration.get("metadata").get("timeseries"), JsonNode.class);
				final List<String> fieldNames = new ArrayList<>();
				timeseries.fieldNames().forEachRemaining(key -> fieldNames.add(key));
				for (int i = 0; i < fieldNames.size(); i++) {
					// Eg) Beta
					final String interventionName = fieldNames.get(i).replaceAll("\"", ",");
					// Eg) "1:0.14, 10:0.1, 20:0.2, 30:0.3"
					final String tempString = timeseries
							.findValue(fieldNames.get(i))
							.toString()
							.replaceAll("\"", "")
							.replaceAll(" ", "");
					final String[] tempList = tempString.split(",");
					for (final String ele : tempList) {
						final Integer timestep = Integer.parseInt(ele.split(":")[0]);
						final Double value = Double.parseDouble(ele.split(":")[1]);
						final Intervention temp = new Intervention();
						temp.setName(interventionName);
						temp.setValue(value);
						temp.setTimestep(timestep);
						interventionList.add(temp);
					}
				}
			}
		} catch (final RuntimeException | IOException e) {
			final String error = String.format(
					"Unable to parse model.configuration.metadata.timeseries for model config id: %s", modelConfigId);
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
		return interventionList;
	}
}
