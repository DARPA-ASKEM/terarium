package software.uncharted.terarium.hmiserver.resources.simulationservice;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import software.uncharted.terarium.hmiserver.utils.Converter;
import software.uncharted.terarium.hmiserver.models.dataservice.Simulation;
import software.uncharted.terarium.hmiserver.models.simulationservice.SimulationRequest;
import software.uncharted.terarium.hmiserver.models.simulationservice.CalibrationRequestJulia;
import software.uncharted.terarium.hmiserver.models.simulationservice.CalibrationRequestCiemss;
import software.uncharted.terarium.hmiserver.models.simulationservice.JobResponse;

import software.uncharted.terarium.hmiserver.proxies.dataservice.SimulationProxy;
import software.uncharted.terarium.hmiserver.proxies.simulationservice.SimulationServiceProxy;
import software.uncharted.terarium.hmiserver.proxies.simulationservice.SimulationCiemssServiceProxy;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/simulation-request")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Simulation Service REST Endpoint")
public class SimulationRequestResource {

	@RestClient
	SimulationServiceProxy simulationServiceProxy;

	@RestClient
	SimulationCiemssServiceProxy simulationCiemssServiceProxy;

	@RestClient
	SimulationProxy simulationProxy;

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
	public JobResponse makeEnsembleSimulateJob(
		final CalibrationRequestCiemss request
	) {
		final JobResponse res = simulationCiemssServiceProxy.makeCalibrateJob(Converter.convertObjectToSnakeCaseJsonNode(request));
		return res;
	}


}
