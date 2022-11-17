package software.uncharted.terarium.hmiserver.resources;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import software.uncharted.terarium.hmiserver.models.Model;
import software.uncharted.terarium.hmiserver.proxies.ModelProxy;
import software.uncharted.terarium.hmiserver.services.ModelService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Path("/api/models")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Model REST Endpoints")
public class ModelResource {

	@Inject
	@RestClient
	ModelProxy modelProxy;

	@Inject
	ModelService modelService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Tag(name = "Get all models")
	public Response getModels() {
		final List<Model> models = new ArrayList<>();
		try {
			// old implementtion based on http-client service works fine
			// final List<Model> rawModelsConverted = modelService.getModels();

			// FIXME: rest-client utilizing proxy causes a problem when using typed objects
			Object rawModels = modelProxy.getModels();
			var rawModelsConverted = new ObjectMapper()
				.registerModule(new Jdk8Module())
				.convertValue(rawModels, new TypeReference<List<Model>>() {});

			for (Model model : (List<Model>)rawModelsConverted) {
				models.add(model);
			}
		} catch (Exception e) {
			e.printStackTrace();
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
