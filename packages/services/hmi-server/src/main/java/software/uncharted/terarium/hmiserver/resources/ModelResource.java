package software.uncharted.terarium.hmiserver.resources;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Model;
import software.uncharted.terarium.hmiserver.proxies.ModelProxy;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/models")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Model REST Endpoints")
public class ModelResource {

	@Inject
	@RestClient
	ModelProxy proxy;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Tag(name = "Get all models")
	public Response getModels() {
		return proxy.getModels();
	}

	@GET
	@Path("/{id}")
	public Response getModel(@PathParam("id") final String id) {
		return proxy.getModel(id);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createModel(final Model newModel) {
		return proxy.createModel(newModel);
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateModel(@PathParam("id") final String id, final Model updatedModel) {
		return proxy.updateModel(id, updatedModel);
	}

	@GET
	@Path("/intermediates/{id}")
	public Response getIntermediate(
		@PathParam("id") final String id
	) {
		return proxy.getIntermediate(id);
	}

	@POST
	@Path("/intermediates")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createIntermediate(
		final Model model
	) {
		return proxy.createIntermediate(model);
	}

	@DELETE
	@Path("/intermediates/{id}")
	public Response deleteIntermediate(
		@PathParam("id") final String id
	) {
		return proxy.deleteIntermediate(id);
	}

	@GET
	@Path("/frameworks/{id}")
	public Response getFramework(
		@PathParam("id") final String id
	) {
		return proxy.getFramework(id);
	}

	@POST
	@Path("/frameworks")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createFramework(
		final Model model
	) {
		return proxy.createFramework(model);
	}

	@DELETE
	@Path("/frameworks/{id}")
	public Response deleteFramework(
		@PathParam("id") final String id
	) {
		return proxy.deleteFramework(id);
	}
}
