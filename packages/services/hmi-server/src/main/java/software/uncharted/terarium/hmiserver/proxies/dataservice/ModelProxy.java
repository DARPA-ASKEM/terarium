package software.uncharted.terarium.hmiserver.proxies.dataservice;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Model;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RegisterRestClient(configKey = "data-service")
@Path("/models")
@Produces(MediaType.APPLICATION_JSON)
public interface ModelProxy {

	@GET
	@Path("/descriptions")
	Response getModels();

	@GET
	@Path("/{id}")
	Response getModel(
		@PathParam("id") String id
	);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	Response createModel(
		Model model
	);

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	Response updateModel(
		@PathParam("id") String id,
		Model plan
	);

	@GET
	@Path("/intermediates/{id}")
	Response getIntermediate(
		@PathParam("id") String id
	);

	@POST
	@Path("/intermediates")
	@Consumes(MediaType.APPLICATION_JSON)
	Response createIntermediate(
		Model model
	);

	@DELETE
	@Path("/intermediates/{id}")
	Response deleteIntermediate(
		@PathParam("id") String id
	);

	@GET
	@Path("/frameworks/{name}")
	Response getFramework(
		@PathParam("name") String id
	);

	@POST
	@Path("/frameworks")
	@Consumes(MediaType.APPLICATION_JSON)
	Response createFramework(
		Model model
	);

	@DELETE
	@Path("/frameworks/{name}")
	Response deleteFramework(
		@PathParam("name") String id
	);
}
