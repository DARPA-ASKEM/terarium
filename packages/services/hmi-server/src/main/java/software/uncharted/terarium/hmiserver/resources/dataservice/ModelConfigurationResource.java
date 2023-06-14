package software.uncharted.terarium.hmiserver.resources.dataservice;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.ModelConfiguration;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ModelConfigurationProxy;
import software.uncharted.terarium.hmiserver.models.dataservice.Model;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("/api/model_configurations")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Model Configuration REST Endpoints")
public class ModelConfigurationResource {
	@Inject
	@RestClient
	ModelConfigurationProxy proxy;

	@GET
	public Response getModelConfigurations() {
		return proxy.getModelConfigurations();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createModelConfiguration(
			String model_id, String name, String description, Model configuration
	) {
		return proxy.createModelConfiguration(model_id, name, description,  configuration);
	}

	@GET
	@Path("/{id}")
	public Response getModelConfiguration(
			@PathParam("id") String id
	) {
		return proxy.getModelConfiguration(id);
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateModelConfiguration(
		@PathParam("id") String id,
		ModelConfiguration config
	) {
		return proxy.updateModelConfiguration(id, config);
	}

	@DELETE
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteModelConfiguration(
		@PathParam("id") String id
	) {
		return proxy.deleteModelConfiguration(id);
	}
}
