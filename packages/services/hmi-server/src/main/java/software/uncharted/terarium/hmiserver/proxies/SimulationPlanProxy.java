package software.uncharted.terarium.hmiserver.proxies;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.SimulationPlan;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@RegisterRestClient(configKey = "data-service")
@Path("/simulations/plans")
@Produces(MediaType.APPLICATION_JSON)
public interface SimulationPlanProxy {

	@GET
	Response getSimulationPlans();

	@GET
	@Path("/{id}")
	Response getSimulationPlan(
		@PathParam("id") String id
	);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	Response createSimulationPlan(
		SimulationPlan plan
	);
}
