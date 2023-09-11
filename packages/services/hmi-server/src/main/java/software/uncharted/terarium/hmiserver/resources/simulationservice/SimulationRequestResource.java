package software.uncharted.terarium.hmiserver.resources.simulationservice;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import software.uncharted.terarium.hmiserver.utils.Converter;
import software.uncharted.terarium.hmiserver.models.dataservice.Simulation;
import software.uncharted.terarium.hmiserver.models.simulationservice.Intervention;
import software.uncharted.terarium.hmiserver.models.simulationservice.SimulationRequest;
import software.uncharted.terarium.hmiserver.models.simulationservice.CalibrationRequestJulia;
import software.uncharted.terarium.hmiserver.models.simulationservice.CalibrationRequestCiemss;
import software.uncharted.terarium.hmiserver.models.simulationservice.EnsembleSimulationCiemssRequest;
import software.uncharted.terarium.hmiserver.models.simulationservice.EnsembleCalibrationCiemssRequest;
import software.uncharted.terarium.hmiserver.models.simulationservice.JobResponse;
import software.uncharted.terarium.hmiserver.models.dataservice.ModelConfiguration;

import software.uncharted.terarium.hmiserver.proxies.dataservice.SimulationProxy;
import software.uncharted.terarium.hmiserver.proxies.simulationservice.SimulationServiceProxy;
import software.uncharted.terarium.hmiserver.proxies.simulationservice.SimulationCiemssServiceProxy;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

import software.uncharted.terarium.hmiserver.proxies.dataservice.ModelConfigurationProxy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Path("/api/simulation-request")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Simulation Service REST Endpoint")
@Slf4j
public class SimulationRequestResource {

	@RestClient
	SimulationServiceProxy simulationServiceProxy;

	@RestClient
	SimulationCiemssServiceProxy simulationCiemssServiceProxy;

	@RestClient
	SimulationProxy simulationProxy;

	@RestClient
	ModelConfigurationProxy modelConfigProxy;

	@GET
	@Path("/{id}")
	public Simulation getSimulation(@PathParam("id") final String id){
		return simulationProxy.getSimulation(id);
	}

	@POST
	@Path("/forecast")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Tag(name = "Make new forecast simulation")
	public Simulation makeForecastRun(
		final SimulationRequest request
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


	@POST
	@Path("ciemss/forecast")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Tag(name = "Make new forecast simulation in CIEMSS")
	public Simulation makeForecastRunCiemss(
		final SimulationRequest request
	) {
		if (request.getInterventions() == null){
			request.setInterventions(getInterventionFromId(request.getModelConfigId()));
		}
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

	@POST
	@Path("/calibrate")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Tag(name = "Create calibrate job")
	public JobResponse makeCalibrateJob(
		final CalibrationRequestJulia request
	) {
		final JobResponse res = simulationServiceProxy.makeCalibrateJob(Converter.convertObjectToSnakeCaseJsonNode(request));

		// Simulation sim = new Simulation();
		// sim.setId(res.getSimulationId());
		// sim.setType("simulation");

		// sim.setExecutionPayload(request);

		// // FIXME: These fiels are arguable unnecessary
		// sim.setStatus("queued");
		// sim.setWorkflowId("dummy");
		// sim.setUserId(0);
		// sim.setProjectId(0);
		// sim.setEngine("sciml");

		// JsonNode jn = Converter.convertObjectToSnakeCaseJsonNode(sim);
		// return simulationProxy.createSimulation(jn);
		return res;
	}

	@POST
	@Path("ciemss/calibrate")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Tag(name = "Create calibrate job")
	public JobResponse makeCalibrateJobCiemss(
		final CalibrationRequestCiemss request
	) {
		final JobResponse res = simulationCiemssServiceProxy.makeCalibrateJob(Converter.convertObjectToSnakeCaseJsonNode(request));

		// Simulation sim = new Simulation();
		// sim.setId(res.getSimulationId());
		// sim.setType("simulation");

		// sim.setExecutionPayload(request);

		// JsonNode jn = Converter.convertObjectToSnakeCaseJsonNode(sim);
		// return simulationProxy.createSimulation(jn);

		return res;
	}

	@POST
	@Path("ciemss/ensemble-simulate")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Tag(name = "Create calibrate job")
	public JobResponse makeEnsembleSimulateCiemssJob(
		final EnsembleSimulationCiemssRequest request
	) {
		final JobResponse res = simulationCiemssServiceProxy.makeEnsembleSimulateCiemssJob(Converter.convertObjectToSnakeCaseJsonNode(request));
		return res;
	}

	@POST
	@Path("ciemss/ensemble-calibrate")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Tag(name = "Create calibrate job")
	public JobResponse makeEnsembleCalibrateCiemssJob(
		final EnsembleCalibrationCiemssRequest request
	) {
		final JobResponse res = simulationCiemssServiceProxy.makeEnsembleCalibrateCiemssJob(Converter.convertObjectToSnakeCaseJsonNode(request));
		return res;
	}

	//Get modelConfigId
	//Check if it has timeseries in its metadata
	//If it does for each element convert it to type Intervention and add it to this.interventions
	//Schema: http://json-schema.org/draft-07/schema#
	public List<Intervention> getInterventionFromId(String modelConfigId){
		List<Intervention> interventionList = new ArrayList<>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			ModelConfiguration modelConfig = modelConfigProxy.getModelConfiguration(modelConfigId);
			JsonNode configuration =  mapper.convertValue(modelConfig.getConfiguration(), JsonNode.class);
			if (configuration.findValue("timeseries") != null){
				JsonNode timeseries = mapper.convertValue(configuration.findValue("timeseries"), JsonNode.class);
				List<String> fieldNames = new ArrayList<>();
				timeseries.fieldNames().forEachRemaining(key -> fieldNames.add(key));
				for (int i = 0; i < fieldNames.size(); i++){
					// Eg) Beta
					String interventionName = fieldNames.get(i).replaceAll("\"",",");
					// Eg) "1:0.14, 10:0.1, 20:0.2, 30:0.3"
					String tempString = timeseries.findValue(fieldNames.get(i)).toString().replaceAll("\"","").replaceAll(" ","");
					String[] tempList = tempString.split(",");
					for (String ele : tempList){
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
		}
		catch (RuntimeException e) {
			log.error("Unable to parse model.configuration.metadata.timeseries for model config id: " + modelConfigId);
			log.error(e.toString());
		}
		return interventionList;
	}
}
