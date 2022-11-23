package software.uncharted.terarium.hmiserver.proxies.dataservice;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.SimulationPlan;
import software.uncharted.terarium.hmiserver.models.dataservice.SimulationRun;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RegisterRestClient(configKey = "data-service")
@Path("/simulations")
@Produces(MediaType.APPLICATION_JSON)
public interface SimulationProxy {

	@GET
	@Path("/plans")
	Response getSimulationPlans();

	@GET
	@Path("/plans/{id}")
	Response getSimulationPlan(
		@PathParam("id") String id
	);

	@POST
	@Path("/plans")
	@Consumes(MediaType.APPLICATION_JSON)
	Response createSimulationPlan(
		SimulationPlan plan
	);

	@GET
	@Path("/results")
	Response getSimulationResults();

	@GET
	@Path("/results/{id}")
	Response getSimulationResult(
		@PathParam("id") String id
	);

	@POST
	@Path("/results")
	@Consumes(MediaType.APPLICATION_JSON)
	Response createSimulationResult(
		SimulationRun run
	);

	@DELETE
	@Path("/results/{id}")
	Response deleteSimulationResult(
		@PathParam("id") String id
	);
}
