package software.uncharted.terarium.hmiserver.resources.dataservice;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Intermediate;
import software.uncharted.terarium.hmiserver.models.dataservice.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.ModelFramework;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ModelProxy;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;


@Path("/api/models")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Model REST Endpoints")
public class ModelResource {

	@Inject
	@RestClient
	ModelProxy proxy;

	@POST
	@Path("/frameworks")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createFramework(
		final ModelFramework framework
	) {
		return proxy.createFramework(framework);
	}

	@GET
	@Path("/frameworks/{name}")
	public Response getFramework(
		@PathParam("name") final String name
	) {
		return proxy.getFramework(name);
	}

	@DELETE
	@Path("/frameworks/{name}")
	public Response deleteFramework(
		@PathParam("name") final String name
	) {
		return proxy.deleteFramework(name);
	}

	@GET
	@Path("/intermediates/{id}")
	public Response getIntermediate(
		@PathParam("id") final String id
	) {
		return proxy.getIntermediate(id);
	}

	@DELETE
	@Path("/intermediates/{id}")
	public Response deleteIntermediate(
		@PathParam("id") final String id
	) {
		return proxy.deleteIntermediate(id);
	}

	@POST
	@Path("/intermediates")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createIntermediate(
		final Intermediate intermediate
	) {
		return proxy.createIntermediate(intermediate);
	}

	@GET
	@Path("/descriptions")
	public Response getDescriptions(

		@DefaultValue("100") @QueryParam("page_size")final Integer pageSize,
		@DefaultValue("0") @QueryParam("page")final Integer page

	) {
		return proxy.getDescriptions(pageSize, page);
	}

	@GET
	@Path("/descriptions/{id}")
	public Response getDescription(
		@PathParam("id") final String id
	) {
		return proxy.getDescription(id);
	}

	@GET
	@Path("/parameters/{id}")
	public Response getParameters(
		@PathParam("id") final String id
	) {
		return proxy.getParameters(id);
	}

	@PUT
	@Path("/parameters/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateParameters(
		@PathParam("id") final String id,
		final Map<String, Object> parameters
	) {
		return proxy.updateParameters(id, parameters);
	}

	@GET
	@Path("/{id}")
	public Response getModel(
		@PathParam("id") final String id
	) {
		return proxy.getModel(id);
	}

	@POST
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateModel(
		@PathParam("id") final String id,
		final Model model
	) {
		return proxy.updateModel(id, model);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createModel(
		final Model model
	) {
		return proxy.createModel(model);
	}
}
