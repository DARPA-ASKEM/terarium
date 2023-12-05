package software.uncharted.terarium.hmiserver.controller.simulationservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.controller.SnakeCaseController;
import software.uncharted.terarium.hmiserver.models.dataservice.Simulation;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelConfiguration;
import software.uncharted.terarium.hmiserver.models.simulationservice.*;
import software.uncharted.terarium.hmiserver.models.simulationservice.parts.Intervention;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ModelConfigurationProxy;
import software.uncharted.terarium.hmiserver.proxies.dataservice.SimulationProxy;
import software.uncharted.terarium.hmiserver.proxies.simulationservice.SimulationCiemssServiceProxy;
import software.uncharted.terarium.hmiserver.proxies.simulationservice.SimulationServiceProxy;
import software.uncharted.terarium.hmiserver.security.Roles;

import java.util.ArrayList;
import java.util.List;


@RequestMapping("/simulation-request")
@RestController
@Slf4j
@RequiredArgsConstructor
//TODO: Once we've moved this off of TDS remove the SnakeCaseController interface and import.
public class SimulationRequestController implements SnakeCaseController {

	private final SimulationServiceProxy simulationServiceProxy;

	private final SimulationCiemssServiceProxy simulationCiemssServiceProxy;

	private final SimulationProxy simulationProxy;

	private final ModelConfigurationProxy modelConfigProxy;

	@Value("${terarium.sciml-queue}")
	private String SCIML_QUEUE;

	@Value("${terarium.queue.suffix:${terarium.userqueue.suffix}}")
	private String queueSuffix;

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<Simulation> getSimulation(
		@PathVariable("id") final String id
	) {
		return ResponseEntity.ok(simulationProxy.getAsset(id).getBody());
	}

	@PostMapping("/forecast")
	@Secured(Roles.USER)
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

		return ResponseEntity.ok(simulationProxy.createAsset(convertObjectToSnakeCaseJsonNode(sim)).getBody());
	}


	@PostMapping("ciemss/forecast")
	@Secured(Roles.USER)
	public ResponseEntity<JsonNode> makeForecastRunCiemss(
		@RequestBody final SimulationRequest request
	) {

		if (request.getInterventions() == null) {
			request.setInterventions(getInterventionFromId(request.getModelConfigId()));
		}

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

		return ResponseEntity.ok(simulationProxy.createAsset(convertObjectToSnakeCaseJsonNode(sim)).getBody());
	}

	@PostMapping("/calibrate")
	@Secured(Roles.USER)
	public ResponseEntity<JobResponse> makeCalibrateJob(
		@RequestBody final CalibrationRequestJulia request
	) {
		return ResponseEntity.ok(simulationServiceProxy.makeCalibrateJob(SCIML_QUEUE+queueSuffix, convertObjectToSnakeCaseJsonNode(request)).getBody());
	}

	@PostMapping("ciemss/calibrate")
	@Secured(Roles.USER)
	public ResponseEntity<JobResponse> makeCalibrateJobCiemss(
		@RequestBody final CalibrationRequestCiemss request
	) {
		return ResponseEntity.ok(simulationCiemssServiceProxy.makeCalibrateJob(convertObjectToSnakeCaseJsonNode(request)).getBody());
	}

	@PostMapping("ciemss/ensemble-simulate")
	@Secured(Roles.USER)
	public ResponseEntity<JobResponse> makeEnsembleSimulateCiemssJob(
		@RequestBody final EnsembleSimulationCiemssRequest request
	) {
		return ResponseEntity.ok(simulationCiemssServiceProxy.makeEnsembleSimulateCiemssJob(convertObjectToSnakeCaseJsonNode(request)).getBody());
	}

	@PostMapping("ciemss/ensemble-calibrate")
	@Secured(Roles.USER)
	public ResponseEntity<JobResponse> makeEnsembleCalibrateCiemssJob(
		@RequestBody final EnsembleCalibrationCiemssRequest request
	) {
		return ResponseEntity.ok(simulationCiemssServiceProxy.makeEnsembleCalibrateCiemssJob(convertObjectToSnakeCaseJsonNode(request)).getBody());
	}


	//Get modelConfigId
	//Check if it has timeseries in its metadata
	//If it does for each element convert it to type Intervention and add it to this.interventions
	//Schema: http://json-schema.org/draft-07/schema#
	private List<Intervention> getInterventionFromId(String modelConfigId) {
		List<Intervention> interventionList = new ArrayList<>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			ModelConfiguration modelConfig = modelConfigProxy.getAsset(modelConfigId).getBody();
			JsonNode configuration = mapper.convertValue(modelConfig.getConfiguration(), JsonNode.class);
			// Parse the values found under the following path:
			// 		AMR -> configuration -> metadata -> timeseries -> parameter name -> value
			// 		EG) "timeseries": {
			//   			"beta": "1:0.05,2:0.04,3:0.01"
			// 			}
			// Into the following format: "interventions": [{"timestep":1,"name":"beta","value":0.05}, {"timestep":2,"name":"beta","value":0.04}, ...]
			//This will later be scrapped after a redesign where our AMR -> configuration -> metadata -> timeseries -> parameter name -> value should be more typed.
			if (configuration.get("metadata").get("timeseries") != null) {
				JsonNode timeseries = mapper.convertValue(configuration.get("metadata").get("timeseries"), JsonNode.class);
				List<String> fieldNames = new ArrayList<>();
				timeseries.fieldNames().forEachRemaining(key -> fieldNames.add(key));
				for (int i = 0; i < fieldNames.size(); i++) {
					// Eg) Beta
					String interventionName = fieldNames.get(i).replaceAll("\"", ",");
					// Eg) "1:0.14, 10:0.1, 20:0.2, 30:0.3"
					String tempString = timeseries.findValue(fieldNames.get(i)).toString().replaceAll("\"", "").replaceAll(" ", "");
					String[] tempList = tempString.split(",");
					for (String ele : tempList) {
						Integer timestep = Integer.parseInt(ele.split(":")[0]);
						Double value = Double.parseDouble(ele.split(":")[1]);
						Intervention temp = new Intervention();
						temp.setName(interventionName);
						temp.setValue(value);
						temp.setTimestep(timestep);
						interventionList.add(temp);
					}
				}
			}
		} catch (RuntimeException e) {
			log.error("Unable to parse model.configuration.metadata.timeseries for model config id: " + modelConfigId);
			log.error(e.toString());
		}
		return interventionList;
	}


}
