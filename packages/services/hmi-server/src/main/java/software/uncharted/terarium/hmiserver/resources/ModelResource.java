package software.uncharted.terarium.hmiserver.resources;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.annotations.Query;
import software.uncharted.terarium.hmiserver.models.Model;
import software.uncharted.terarium.hmiserver.proxies.ModelProxy;

import javax.inject.Inject;
import javax.print.attribute.standard.Media;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

@Path("/api/model")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Model REST Endpoints")
public class ModelResource {

	@Inject
	@RestClient
	ModelProxy modelProxy;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Tag(name = "Get all models")
	public Response getModels() {
		final List<Model> models = modelProxy.getModels();
		if (models.isEmpty()) {
			return Response.noContent().build();
		}
		return Response.ok(models).build();
	}

	@GET
	@Path("/{id}")
	public Response getModel(@PathParam("id") final Long id) {
		final Model model = modelProxy.getModel(id);

		if (model == null)
			throw new WebApplicationException(Response.Status.NOT_FOUND);

		return Response.ok(model).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createModel(final Model newModel) {
		final Model model = modelProxy.createModel(newModel);
		return Response.created(URI.create("/api/models/" + model.id)).build();
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateModel(@PathParam("id") final Long id, final Model updatedModel) {
		if (modelProxy.getModel(id) == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

		final Model model = modelProxy.updateModel(id, updatedModel);

		if (model == null) {
			return Response.noContent().build();
		}
		return Response.ok(model).build();
	}

	@DELETE
	@Path("/{id}")
	public Response deleteModel(@PathParam("id") final Long id) {
		if (!modelProxy.deleteModel(id)) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

		return Response.ok().build();
	}

}
