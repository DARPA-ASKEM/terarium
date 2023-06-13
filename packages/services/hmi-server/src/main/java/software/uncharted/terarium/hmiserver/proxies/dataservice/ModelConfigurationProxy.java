package software.uncharted.terarium.hmiserver.proxies.dataservice;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.ModelConfiguration;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RegisterRestClient(configKey = "data-service")
@Path("/model_configurations")
@Produces(MediaType.APPLICATION_JSON)
public interface ModelConfigurationProxy {

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	Response getModelConfigurations();

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	Response createModelConfiguration(
		ModelConfiguration config
	);

	@GET
	@Path("/{id}")
	Response getModelConfiguration(
		@PathParam("id") String id
	);

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	Response updateModelConfiguration(
		@PathParam("id") String id
	);

	@DELETE
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	Response deleteModelConfiguration(
		@PathParam("id") String id
	);
}
