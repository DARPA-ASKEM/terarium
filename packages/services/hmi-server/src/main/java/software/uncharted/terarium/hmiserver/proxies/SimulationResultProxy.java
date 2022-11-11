package software.uncharted.terarium.hmiserver.proxies;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.models.SimulationRun;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@RegisterRestClient
@Path("/results")
@Produces(MediaType.APPLICATION_JSON)
public interface SimulationResultProxy {

	@GET
	List<SimulationRun> getSimulationResults();

	@GET
	@Path("/{id}")
	SimulationRun getSimulationResult(
		@QueryParam("id") Long id
	);

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	Boolean deleteSimulationResult(
		Long id
	);
}
