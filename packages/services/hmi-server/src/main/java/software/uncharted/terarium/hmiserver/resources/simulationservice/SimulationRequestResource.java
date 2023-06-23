package software.uncharted.terarium.hmiserver.resources.simulationservice;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import software.uncharted.terarium.hmiserver.utils.Converter;
import software.uncharted.terarium.hmiserver.models.dataservice.Simulation;
import software.uncharted.terarium.hmiserver.models.simulationservice.SimulationRequest;
import software.uncharted.terarium.hmiserver.models.simulationservice.CalibrationRequest;
import software.uncharted.terarium.hmiserver.models.simulationservice.JobResponse;

import software.uncharted.terarium.hmiserver.proxies.dataservice.SimulationProxy;
import software.uncharted.terarium.hmiserver.proxies.simulationservice.SimulationServiceProxy;

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
	@Path("/calibrate")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Tag(name = "Create calibrate job")
	public Response makeCalibrateJob(
		final CalibrationRequest calibrationRequest
	) {
		final JobResponse res = simulationServiceProxy.makeCalibrateJob(calibrationRequest);
		return null;
	}
}
