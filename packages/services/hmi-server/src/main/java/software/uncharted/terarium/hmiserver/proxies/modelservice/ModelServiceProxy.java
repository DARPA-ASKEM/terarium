package software.uncharted.terarium.hmiserver.proxies.modelservice;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import software.uncharted.terarium.hmiserver.models.modelservice.Graph;
import software.uncharted.terarium.hmiserver.models.modelservice.SimulateParams;
import software.uncharted.terarium.hmiserver.models.modelservice.ModelCompositionParams;

@RegisterRestClient(configKey = "model-service")
@Produces(MediaType.APPLICATION_JSON)
@Path("api/models")
public interface ModelServiceProxy {
	@PUT
	Response createModel();

	@POST
	@Path("/{modelId}")
	@Consumes(MediaType.APPLICATION_JSON)
	Response addModelParts(
		@PathParam("modelId") String modelId,
		Graph graph
	);

	@GET
	@Path("/{modelId}/json")
	@Consumes(MediaType.APPLICATION_JSON)
	Response getModelJSON(
		@PathParam("modelId") String modelId
	);

	@POST
	@Path("/{modelId}/simulate")
	@Consumes(MediaType.APPLICATION_JSON)
	Response simulate(
		@PathParam("modelId") String modelId,
		SimulateParams params
	);

	@POST
	@Path("/model-composition")
	@Consumes(MediaType.APPLICATION_JSON)
	Response modelComposition(
		ModelCompositionParams params
	);

	@GET
	@Path("/stratify/{modelA}/{modelB}/{typeModel}")
	@Consumes(MediaType.APPLICATION_JSON)
	Response stratify(
		@PathParam("modelA") String modelA,
		@PathParam("modelB") String modelB,
		@PathParam("typeModel") String typeModel
	);

}
