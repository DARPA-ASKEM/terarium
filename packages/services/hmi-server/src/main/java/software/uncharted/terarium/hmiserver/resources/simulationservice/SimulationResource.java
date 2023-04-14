package software.uncharted.terarium.hmiserver.resources.simulationservice;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import software.uncharted.terarium.hmiserver.models.dataservice.Simulation;
import software.uncharted.terarium.hmiserver.models.simulationservice.SimulationParams;
import software.uncharted.terarium.hmiserver.proxies.dataservice.SimulationProxy;
import software.uncharted.terarium.hmiserver.proxies.simulationservice.SimulationServiceProxy;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/simulation")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Simulation Service REST Endpoint")
public class SimulationResource {

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
	public Simulation createSimulation(final Simulation simulation){
		return simulationProxy.createSimulation(simulation);
	}

	@PATCH
	@Path("/{id}")
	public Simulation updateSimulation(@PathParam("id") final String id, final Simulation simulation){
		return simulationProxy.updateSimulation(id, simulation);
	}

	@DELETE
	@Path("/{id}")
	public String deleteSimulation(@PathParam("id") final String id){
		return simulationProxy.deleteSimulation(id);
	}

	@POST
	@Path("/forecast")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Tag(name = "Make new forecast simulation")
	public Response makeForecastRun(
		final SimulationParams simulationParams
	) {
		return simulationServiceProxy.makeForecastRun(simulationParams);
	}

	@GET
	@Path("/{runId}/status")
	@Produces(MediaType.APPLICATION_JSON)
	@Tag(name = "Get simulation status associated with run ID")
	public Response getRunStatus(
		@PathParam("runId") final String runId
	) {
		return simulationServiceProxy.getRunStatus(runId);
	}

	@GET
	@Path("/{runId}/result")
	@Produces(MediaType.APPLICATION_JSON)
	@Tag(name = "Get simulation result associated with run ID")
	public Response getRunResult(
		@PathParam("runId") final String runId
	) {
		return simulationServiceProxy.getRunResult(runId);
	}
}
