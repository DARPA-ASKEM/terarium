package software.uncharted.terarium.hmiserver.resources.dataservice;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.ModelConfiguration;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ModelConfigurationProxy;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/api/model_configurations")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Model Cnfiguration REST Endpoints")
public class ModelConfigurationResource {

	@Inject
	@RestClient
	ModelConfigurationProxy proxy;

	@GET
	Response getModelConfigurations() {
		return proxy.getModelConfigurations();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	Response createModelConfiguration(
			ModelConfiguration config
	) {
		return proxy.createModelConfiguration(config);
	}

	@GET
	@Path("/{id}")
	Response getModelConfiguration(
			@PathParam("id") String id
	) {
		return proxy.getModelConfiguration(id);
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	Response updateModelConfiguration(
		@PathParam("id") String id,
		ModelConfiguration config
	) {
		return proxy.updateModelConfiguration(id, config);
	}

	@DELETE
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	Response deleteModelConfiguration(
		@PathParam("id") String id
	) {
		return proxy.deleteModelConfiguration(id);
	}
}
