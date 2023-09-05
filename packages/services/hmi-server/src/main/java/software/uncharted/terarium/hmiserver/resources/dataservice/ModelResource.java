package software.uncharted.terarium.hmiserver.resources.dataservice;


import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.ModelFramework;
import software.uncharted.terarium.hmiserver.models.dataservice.ModelOperationCopy;
import software.uncharted.terarium.hmiserver.models.dataservice.ModelConfiguration;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ModelProxy;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

import static io.smallrye.jwt.config.ConfigLogging.log;

@Path("/api/models")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Model REST Endpoints")
public class ModelResource {

	@ConfigProperty(name = "mira-metaregistry/mp-rest/url")
	String metaRegistryURL;

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
	@Path("/descriptions")
	public Response getDescriptions(
		@DefaultValue("1000") @QueryParam("page_size") final Integer pageSize,
		@DefaultValue("0") @QueryParam("page") final Integer page
	) {
		return proxy.getDescriptions(pageSize, page);
	}

	@GET
	@Path("/{id}/descriptions")
	public Response getDescription(
		@PathParam("id") final String id
	) {
		return proxy.getDescription(id);
	}

	/**
	 * Get a Model from the data-service
	 * Return Model
	 */
	@GET
	@Path("/{id}")
	@APIResponses({
		@APIResponse(responseCode = "500", description = "An error occurred retrieving the model"),
		@APIResponse(responseCode = "204", description = "Request received successfully, but there are no model with this id")
	})
	public Response getModel(
		@PathParam("id") final String id
	) {
		Model model;

		// Fetch the model from the data-service
		try {
			model = proxy.getModel(id);
		} catch (RuntimeException e) {
			log.error("Unable to get the model" + id, e);
			return Response
				.status(Response.Status.INTERNAL_SERVER_ERROR)
				.build();
		}

		if (model == null) {
			return Response.noContent().build();
		}

		// Return the model
		return Response
			.status(Response.Status.OK)
			.entity(model)
			.build();
	}

	@PUT
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

	@POST
	@Path("/{id}/operation-copy")
	public Response saveAsNewModel(
		@PathParam("id") final String id,
		final ModelOperationCopy modelOperationCopy) {
		return proxy.copyModel(modelOperationCopy);
	}

	@GET
	@Path("/{id}/model_configurations")
	public Response getModelConfigurations(
			@PathParam("id") String id
	) {
		final List<ModelConfiguration> configs = proxy.getModelConfigurations(id, 100);
		return Response
			.status(Response.Status.OK)
			.entity(configs)
			.build();
	}
}
