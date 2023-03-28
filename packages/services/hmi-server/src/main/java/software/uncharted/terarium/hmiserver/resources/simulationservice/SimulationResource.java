package software.uncharted.terarium.hmiserver.resources.simulationservice;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import software.uncharted.terarium.hmiserver.models.simulationservice.SimulationParams;
import software.uncharted.terarium.hmiserver.proxies.simulationservice.SimulationServiceProxy;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/simulation-service")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Simulation Service REST Endpoint")
public class SimulationResource {

	@RestClient
	SimulationServiceProxy proxy;

	@POST
	@Path("/forecast")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Tag(name = "Make new forecast simulation")
	public Response makeForecastRun(
		final SimulationParams simulationParams
	) {
		System.out.println(simulationParams);
		System.out.println(proxy);
		return proxy.makeForecastRun(simulationParams);
	}

	@GET
	@Path("/{runId}/status")
	@Produces(MediaType.APPLICATION_JSON)
	@Tag(name = "Get simulation status associated with run ID")
	public Response getRunStatus(
		@PathParam("runId") final String runId
	) {
		return proxy.getRunStatus(runId);
	}

	@GET
	@Path("/{runId}/result")
	@Produces(MediaType.APPLICATION_JSON)
	@Tag(name = "Get simulation result associated with run ID")
	public Response getRunResult(
		@PathParam("runId") final String runId
	) {
		return proxy.getRunResult(runId);
	}
}
