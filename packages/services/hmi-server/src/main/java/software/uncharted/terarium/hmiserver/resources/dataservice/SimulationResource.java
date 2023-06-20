package software.uncharted.terarium.hmiserver.resources.dataservice;


import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
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
	@Path("/{id}")
	public Response getSimulation(
		@PathParam("id") final String id
	) {
		return proxy.getSimulation(id);
	}
}
