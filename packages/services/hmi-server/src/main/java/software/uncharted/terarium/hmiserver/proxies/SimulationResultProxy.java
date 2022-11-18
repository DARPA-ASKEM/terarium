package software.uncharted.terarium.hmiserver.proxies;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.models.simulation.SimulationRun;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RegisterRestClient(configKey = "data-service")
@Path("/simulations/results")
@Produces(MediaType.APPLICATION_JSON)
public interface SimulationResultProxy {

	@GET
	Response getSimulationResults();

	@GET
	@Path("/{id}")
	Response getSimulationResult(
		@PathParam("id") String id
	);

	@POST
	Response createSimulationResult(
		SimulationRun run
	);

	@DELETE
	@Path("/{id}")
	Response deleteSimulationResult(
		@PathParam("id") String id
	);
}
