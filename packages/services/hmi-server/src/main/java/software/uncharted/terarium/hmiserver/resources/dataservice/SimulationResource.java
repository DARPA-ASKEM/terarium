package software.uncharted.terarium.hmiserver.resources.dataservice;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.SimulationPlan;
import software.uncharted.terarium.hmiserver.models.dataservice.SimulationRun;
import software.uncharted.terarium.hmiserver.models.dataservice.SimulationRunDescription;
import software.uncharted.terarium.hmiserver.proxies.dataservice.SimulationProxy;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

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
	public Response getSimulationPlans(
		@DefaultValue("100") @QueryParam("page_size") final Integer pageSize,
		@DefaultValue("0") @QueryParam("page") final Integer page
	) {
		return proxy.getSimulationPlans(pageSize, page);
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
	@Path("/plans/{id}")
	public Response getSimulationPlan(
		@PathParam("id") final String id
	) {
		return proxy.getSimulationPlan(id);
	}

	@GET
	@Path("/runs/descriptions")
	public Response getSimulationRunDescriptions(
		@DefaultValue("100") @QueryParam("page_size") final Integer pageSize,
		@DefaultValue("0") @QueryParam("page") final Integer page
	) {
		return proxy.getSimulationRunDescriptions(pageSize, page);
	}

	@POST
	@Path("/runs/descriptions")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createSimulationRunFromDescription(
		final SimulationRunDescription description
	) {
		return proxy.createSimulationRunFromDescription(description);
	}

	@GET
	@Path("/runs/descriptions/{id}")
	public Response getSimulationRunDescription(
		@PathParam("id") final String id
	) {
		return proxy.getSimulationRunDescription(id);
	}

	@GET
	@Path("/runs/parameters/{id}")
	public Response getSimulationRunParameters(
		@PathParam("id") final String id
	) {
		return proxy.getSimulationRunParameters(id);
	}

	@PUT
	@Path("/runs/descriptions")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateSimulationRunParameters(
		final Map<String, String> parameters
	) {
		return proxy.updateSimulationRunParameters(parameters);
	}

	@GET
	@Path("/runs/{id}")
	public Response getSimulationRun(
		@PathParam("id") final String id
	) {
		return proxy.getSimulationRun(id);
	}

	@POST
	@Path("/runs")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createSimulationRun(
		final SimulationRun run
	) {
		return proxy.createSimulationRun(run);
	}
}
