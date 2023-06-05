package software.uncharted.terarium.hmiserver.proxies.dataservice;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Simulation;
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
	@Path("/{id}")
	Simulation getSimulation(
		@PathParam("id") String id
	);

	@POST
	Simulation createSimulation(
		Simulation simulation
	);

	@PATCH
	@Path("/{id}")
	Simulation updateSimulation(
		@PathParam("id") String id,
		Simulation simulation
	);

	@DELETE
	@Path("/{id}")
	String deleteSimulation(
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
	@Path("/runs/{id}/descriptions")
	Response getSimulationRunDescription(
		@PathParam("id") String id
	);

	@GET
	@Path("/runs/{id}/parameters")
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
