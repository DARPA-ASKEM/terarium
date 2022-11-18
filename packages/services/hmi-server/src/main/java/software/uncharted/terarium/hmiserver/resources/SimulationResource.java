package software.uncharted.terarium.hmiserver.resources;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.simulation.SimulationPlan;
import software.uncharted.terarium.hmiserver.proxies.SimulationPlanProxy;
import software.uncharted.terarium.hmiserver.proxies.SimulationResultProxy;

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
	SimulationPlanProxy planProxy;

	@Inject
	@RestClient
	SimulationResultProxy resultsProxy;

	@GET
	@Path("/plans")
	public Response getSimulationPlans() {
		return planProxy.getSimulationPlans();
	}

	@GET
	@Path("/plans/{id}")
	public Response getSimulationPlan(
		@QueryParam("id") final String id
	) {
		return planProxy.getSimulationPlan(id);
	}

	@POST
	@Path("/plans")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createSimulationPlan(
		final SimulationPlan newPlan
	) {
		return planProxy.createSimulationPlan(newPlan);
	}

	@GET
	@Path("/results")
	public Response getSimulationResults() {
		return resultsProxy.getSimulationResults();
	}

	@GET
	@Path("/results/{id}")
	public Response getSimulationResult(
		@PathParam("id") final String id
	) {
		return resultsProxy.getSimulationResult(id);
	}

	@DELETE
	@Path("/results/{id}")
	public Response deleteSimulationResult(
		@PathParam("id") final String id
	) {
		return resultsProxy.deleteSimulationResult(id);
	}
}
