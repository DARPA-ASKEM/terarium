package software.uncharted.terarium.hmiserver.resources.dataservice;


import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.SimulationRun;
import software.uncharted.terarium.hmiserver.models.dataservice.SimulationRunDescription;
import software.uncharted.terarium.hmiserver.proxies.dataservice.SimulationProxy;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("/api/simulations")

@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Simulation REST Endpoints")
public class SimulationResource {

	@Inject
	@RestClient
	SimulationProxy proxy;


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
	@Path("/runs/{id}/descriptions")
	public Response getSimulationRunDescription(
		@PathParam("id") final String id
	) {
		return proxy.getSimulationRunDescription(id);
	}

	@GET
	@Path("/runs/{id}/parameters")
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
