package software.uncharted.terarium.hmiserver.proxies;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.models.SimulationPlan;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@RegisterRestClient(configKey = "data-service")
@Path("/plans")
@Produces(MediaType.APPLICATION_JSON)
public interface SimulationPlanProxy {

	@GET
	List<SimulationPlan> getSimulationPlans();

	@GET
	@Path("/{id}")
	SimulationPlan getSimulationPlan(
		@PathParam("id") Long id
	);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	SimulationPlan createSimulationPlan(
		SimulationPlan plan
	);

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	SimulationPlan updateSimulationPlan(
		@PathParam("id") Long id,
		SimulationPlan plan
	);

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	Boolean deleteSimulationPlan(
		@PathParam("id") Long id
	);
}
