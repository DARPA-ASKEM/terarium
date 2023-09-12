package software.uncharted.terarium.hmiserver.controller.simulationservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;
import software.uncharted.terarium.hmiserver.utils.Converter;
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

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RequestMapping("/simulation-request")
@RestController
@Slf4j
public class SimulationRequestResource {

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
		return simulationProxy.getSimulation(id);
	}

	@PostMapping("/forecast")
	public ResponseEntity<Simulation> makeForecastRun(
		@RequestParam(name = "request") final SimulationRequest request
	) {
		final JobResponse res = simulationServiceProxy.makeForecastRun(Converter.convertObjectToSnakeCaseJsonNode(request));

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

		JsonNode jn = Converter.convertObjectToSnakeCaseJsonNode(sim);
		return simulationProxy.createSimulation(jn);
	}


	@PostMapping("ciemss/forecast")
	public ResponseEntity<Simulation> makeForecastRunCiemss(
		@RequestParam(name = "request") final SimulationRequest request
	) {
		final JobResponse res = simulationCiemssServiceProxy.makeForecastRun(Converter.convertObjectToSnakeCaseJsonNode(request));

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

		JsonNode jn = Converter.convertObjectToSnakeCaseJsonNode(sim);
		return simulationProxy.createSimulation(jn);
	}

	@PostMapping("/calibrate")
	public ResponseEntity<JobResponse> makeCalibrateJob(
		@RequestParam(name = "request") final CalibrationRequestJulia request
	) {
		return simulationServiceProxy.makeCalibrateJob(Converter.convertObjectToSnakeCaseJsonNode(request));
	}

	@PostMapping("ciemss/calibrate")
	public ResponseEntity<JobResponse> makeCalibrateJobCiemss(
		@RequestParam(name = "request") final CalibrationRequestCiemss request
	) {
		return simulationCiemssServiceProxy.makeCalibrateJob(Converter.convertObjectToSnakeCaseJsonNode(request));
	}

	@PostMapping("ciemss/ensemble-simulate")
	public ResponseEntity<JobResponse> makeEnsembleSimulateCiemssJob(
		@RequestParam(name = "request") final EnsembleSimulationCiemssRequest request
	) {
		return simulationCiemssServiceProxy.makeEnsembleSimulateCiemssJob(Converter.convertObjectToSnakeCaseJsonNode(request));
	}

	@PostMapping("ciemss/ensemble-calibrate")
	public ResponseEntity<JobResponse> makeEnsembleCalibrateCiemssJob(
		@RequestParam(name = "request") final EnsembleCalibrationCiemssRequest request
	) {
		return simulationCiemssServiceProxy.makeEnsembleCalibrateCiemssJob(Converter.convertObjectToSnakeCaseJsonNode(request));
	}


}
