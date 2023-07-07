package software.uncharted.terarium.hmiserver.proxies.dataservice;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.ModelFramework;
import software.uncharted.terarium.hmiserver.models.dataservice.ModelOperationCopy;
import software.uncharted.terarium.hmiserver.models.dataservice.ModelConfiguration;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.List;

@RegisterRestClient(configKey = "data-service")
@Path("/models")
@Produces(MediaType.APPLICATION_JSON)
public interface ModelProxy {

	@POST
	@Path("/frameworks")
	@Consumes(MediaType.APPLICATION_JSON)
	Response createFramework(
		ModelFramework framework
	);

	@GET
	@Path("/frameworks/{name}")
	Response getFramework(
		@PathParam("name") String name
	);

	@DELETE
	@Path("/frameworks/{name}")
	Response deleteFramework(
		@PathParam("name") String name
	);

	@GET
	@Path("/descriptions")
	Response getDescriptions(
		@DefaultValue("100") @QueryParam("page_size") Integer pageSize,
		@DefaultValue("0") @QueryParam("page") Integer page
	);

	@GET
	@Path("/{id}/descriptions")
	Response getDescription(
		@PathParam("id") String id
	);

	@GET
	@Path("/{id}")
	Model getModel(
		@PathParam("id") String id
	);

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	Response updateModel(
		@PathParam("id") String id,
		Model model
	);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	Response createModel(
		Model model
	);

	@POST
	@Path("/opts/copy")
	@Consumes(MediaType.APPLICATION_JSON)
	Response copyModel(
		ModelOperationCopy modelOperationCopy
	);

	@GET
	@Path("/{id}/model_configurations")
	List<ModelConfiguration> getModelConfigurations(
			@PathParam("id") String id
	);
}
