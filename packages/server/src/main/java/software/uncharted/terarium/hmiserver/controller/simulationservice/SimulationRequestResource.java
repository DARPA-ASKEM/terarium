package software.uncharted.terarium.hmiserver.controller.simulationservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;

import software.uncharted.terarium.hmiserver.controller.SnakeCaseResource;
import software.uncharted.terarium.hmiserver.models.dataservice.Simulation;
import software.uncharted.terarium.hmiserver.models.simulationservice.SimulationRequest;
import software.uncharted.terarium.hmiserver.models.simulationservice.CalibrationRequestJulia;
import software.uncharted.terarium.hmiserver.models.simulationservice.CalibrationRequestCiemss;
import software.uncharted.terarium.hmiserver.models.simulationservice.EnsembleSimulationCiemssRequest;
import software.uncharted.terarium.hmiserver.models.simulationservice.EnsembleCalibrationCiemssRequest;
import software.uncharted.terarium.hmiserver.models.simulationservice.JobResponse;

import software.uncharted.terarium.hmiserver.proxies.dataservice.SimulationProxy;
import software.uncharted.terarium.hmiserver.proxies.simulationservice.SimulationServiceProxy;
import software.uncharted.terarium.hmiserver.proxies.simulationservice.SimulationCiemssServiceProxy;


@RequestMapping("/simulation-request")
@RestController
@Slf4j
public class SimulationRequestResource implements SnakeCaseResource {

	@Autowired
	private SimulationServiceProxy simulationServiceProxy;

	@Autowired
	private SimulationCiemssServiceProxy simulationCiemssServiceProxy;

	@Autowired
	private SimulationProxy simulationProxy;

	@GetMapping("/{id}")
	public ResponseEntity<Simulation> getSimulation(
		@PathVariable("id") final String id
	){
		return simulationProxy.getAsset(id);
	}

	@PostMapping("/forecast")
	public ResponseEntity<JsonNode> makeForecastRun(
		@RequestBody final SimulationRequest request
	) {
		final JobResponse res = simulationServiceProxy.makeForecastRun(convertObjectToSnakeCaseJsonNode(request)).getBody();

		Simulation sim = new Simulation();
		sim.setId(res.getSimulationId());
		sim.setType("simulation");

		// FIXME: engine is set twice, talk to TDS
		request.setEngine("sciml");

		sim.setExecutionPayload(request);

		// FIXME: These fiels are arguable unnecessary
		sim.setStatus("queued");
		sim.setWorkflowId("dummy");
		sim.setUserId(0);
		sim.setProjectId(0);
		sim.setEngine("sciml");

;
		return simulationProxy.createAsset(convertObjectToSnakeCaseJsonNode(sim));
	}


	@PostMapping("ciemss/forecast")
	public ResponseEntity<JsonNode> makeForecastRunCiemss(
		@RequestBody final SimulationRequest request
	) {
		final JobResponse res = simulationCiemssServiceProxy.makeForecastRun(convertObjectToSnakeCaseJsonNode(request)).getBody();

		Simulation sim = new Simulation();
		sim.setId(res.getSimulationId());
		sim.setType("simulation");

		// FIXME: engine is set twice, talk to TDS
		request.setEngine("ciemss");

		sim.setExecutionPayload(request);

		// FIXME: These fiels are arguable unnecessary
		sim.setStatus("queued");
		sim.setWorkflowId("dummy");
		sim.setUserId(0);
		sim.setProjectId(0);
		sim.setEngine("ciemss");

		return simulationProxy.createAsset(convertObjectToSnakeCaseJsonNode(sim));
	}

	@PostMapping("/calibrate")
	public ResponseEntity<JobResponse> makeCalibrateJob(
		@RequestBody final CalibrationRequestJulia request
	) {
		return simulationServiceProxy.makeCalibrateJob(convertObjectToSnakeCaseJsonNode(request));
	}

	@PostMapping("ciemss/calibrate")
	public ResponseEntity<JobResponse> makeCalibrateJobCiemss(
		@RequestBody final CalibrationRequestCiemss request
	) {
		return simulationCiemssServiceProxy.makeCalibrateJob(convertObjectToSnakeCaseJsonNode(request));
	}

	@PostMapping("ciemss/ensemble-simulate")
	public ResponseEntity<JobResponse> makeEnsembleSimulateCiemssJob(
		@RequestBody final EnsembleSimulationCiemssRequest request
	) {
		return simulationCiemssServiceProxy.makeEnsembleSimulateCiemssJob(convertObjectToSnakeCaseJsonNode(request));
	}

	@PostMapping("ciemss/ensemble-calibrate")
	public ResponseEntity<JobResponse> makeEnsembleCalibrateCiemssJob(
		@RequestBody final EnsembleCalibrationCiemssRequest request
	) {
		return simulationCiemssServiceProxy.makeEnsembleCalibrateCiemssJob(convertObjectToSnakeCaseJsonNode(request));
	}


}
