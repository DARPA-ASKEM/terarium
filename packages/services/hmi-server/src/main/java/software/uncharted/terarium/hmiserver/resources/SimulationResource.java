package software.uncharted.terarium.hmiserver.resources;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.SimulationPlan;
import software.uncharted.terarium.hmiserver.models.dataservice.SimulationRun;
import software.uncharted.terarium.hmiserver.proxies.SimulationProxy;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/simulations")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Simulation REST Endpoints")
public class SimulationResource {

	@Inject
	@RestClient
	SimulationProxy proxy;

	@GET
	@Path("/plans")
	public Response getSimulationPlans() {
		return proxy.getSimulationPlans();
	}

	@GET
	@Path("/plans/{id}")
	public Response getSimulationPlan(
		@PathParam("id") final String id
	) {
		return proxy.getSimulationPlan(id);
	}

	@POST
	@Path("/plans")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createSimulationPlan(
		final SimulationPlan plan
	) {
		return proxy.createSimulationPlan(plan);
	}

	@GET
	@Path("/results")
	public Response getSimulationResults() {
		return proxy.getSimulationResults();
	}

	@GET
	@Path("/results/{id}")
	public Response getSimulationResult(
		@PathParam("id") final String id
	) {
		return proxy.getSimulationResult(id);
	}

	@POST
	@Path("/results")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createSimulationResult(
		final SimulationRun run
	) {
		return proxy.createSimulationResult(run);
	}

	@DELETE
	@Path("/results/{id}")
	public Response deleteSimulationResult(
		@PathParam("id") final String id
	) {
		return proxy.deleteSimulationResult(id);
	}
}
