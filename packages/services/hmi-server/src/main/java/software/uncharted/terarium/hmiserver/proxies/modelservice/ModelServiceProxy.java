package software.uncharted.terarium.hmiserver.proxies.modelservice;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import software.uncharted.terarium.hmiserver.models.modelservice.Graph;
import software.uncharted.terarium.hmiserver.models.modelservice.SimulateParams;

@RegisterRestClient(configKey = "model-service")
@Produces(MediaType.APPLICATION_JSON)
public interface ModelServiceProxy {
	@PUT
	@Path("api/models")
	Response createModel();

	@POST
	@Path("api/models/{modelId}")
	@Consumes(MediaType.APPLICATION_JSON)
	Response addModelParts(
		@PathParam("modelId") String modelId,
		Graph graph
	);

	@GET
	@Path("api/models/{modelId}/json")
	@Consumes(MediaType.APPLICATION_JSON)
	Response getModelJSON(
		@PathParam("modelId") String modelId
	);

	@POST
	@Path("api/models/{modelId}/simulate")
	@Consumes(MediaType.APPLICATION_JSON)
	Response simulate(
		@PathParam("modelId") String modelId,
		SimulateParams params
	);
}
