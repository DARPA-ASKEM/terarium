package software.uncharted.terarium.hmiserver.proxies;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.models.Project;
import software.uncharted.terarium.hmiserver.models.SimulationPlan;
import software.uncharted.terarium.hmiserver.models.User;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@RegisterRestClient
@Path("/plans")
@Produces(MediaType.APPLICATION_JSON)
public interface SimulationProxy {

	@GET
	List<SimulationPlan> getSimulationPlans();

	@GET
	@Path("/{id}")
	Project getSimulationPlan(
		@QueryParam("id") Long id
	);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	Project createSimulationPlan(
		SimulationPlan plan
	);

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	Project updateSimulationPlan(
		Long id,
		SimulationPlan plan
	);

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	Boolean deleteSimulationPlan(
		User user,
		Long id
	);
}
