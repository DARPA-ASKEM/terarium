package software.uncharted.terarium.hmiserver.proxies.dataservice;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.SimulationPlan;
import software.uncharted.terarium.hmiserver.models.dataservice.SimulationRun;
import software.uncharted.terarium.hmiserver.models.dataservice.SimulationRunDescription;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@RegisterRestClient(configKey = "data-service")
@Path("/simulations")
@Produces(MediaType.APPLICATION_JSON)
public interface SimulationProxy {

	@GET
	@Path("/plans")
	Response getSimulationPlans(
		@DefaultValue("100") @QueryParam("page_size") Integer pageSize,
		@DefaultValue("0") @QueryParam("page") Integer page
	);

	@POST
	@Path("/plans")
	@Consumes(MediaType.APPLICATION_JSON)
	Response createSimulationPlan(
		SimulationPlan plan
	);

	@GET
	@Path("/plans/{id}")
	Response getSimulationPlan(
		@PathParam("id") String id
	);

	@GET
	@Path("/runs/descriptions")
	Response getSimulationRunDescriptions(
		@DefaultValue("100") @QueryParam("page_size") Integer pageSize,
		@DefaultValue("0") @QueryParam("page") Integer page
	);

	@POST
	@Path("/runs/descriptions")
	@Consumes(MediaType.APPLICATION_JSON)
	Response createSimulationRunFromDescription(
		SimulationRunDescription description
	);

	@GET
	@Path("/runs/descriptions/{id}")
	Response getSimulationRunDescription(
		@PathParam("id") String id
	);

	@GET
	@Path("/runs/parameters/{id}")
	Response getSimulationRunParameters(
		@PathParam("id") String id
	);

	@PUT
	@Path("/runs/descriptions")
	@Consumes(MediaType.APPLICATION_JSON)
	Response updateSimulationRunParameters(
		Map<String, String> parameters
	);

	@GET
	@Path("/runs/{id}")
	Response getSimulationRun(
		@PathParam("id") String id
	);

	@POST
	@Path("/runs")
	@Consumes(MediaType.APPLICATION_JSON)
	Response createSimulationRun(
		SimulationRun run
	);
}
