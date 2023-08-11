package software.uncharted.terarium.hmiserver.proxies.dataservice;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.annotations.LogRestClientTime;
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
	@LogRestClientTime
	Response createFramework(
		ModelFramework framework
	);

	@GET
	@Path("/frameworks/{name}")
	@LogRestClientTime
	Response getFramework(
		@PathParam("name") String name
	);

	@DELETE
	@Path("/frameworks/{name}")
	@LogRestClientTime
	Response deleteFramework(
		@PathParam("name") String name
	);

	@GET
	@Path("/descriptions")
	@LogRestClientTime
	Response getDescriptions(
		@DefaultValue("100") @QueryParam("page_size") Integer pageSize,
		@DefaultValue("0") @QueryParam("page") Integer page
	);

	@GET
	@Path("/{id}/descriptions")
	@LogRestClientTime
	Response getDescription(
		@PathParam("id") String id
	);

	@GET
	@Path("/{id}")
	@LogRestClientTime
	Model getModel(
		@PathParam("id") String id
	);

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@LogRestClientTime
	Response updateModel(
		@PathParam("id") String id,
		Model model
	);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@LogRestClientTime
	Response createModel(
		Model model
	);

	@POST
	@Path("/opts/copy")
	@Consumes(MediaType.APPLICATION_JSON)
	@LogRestClientTime
	Response copyModel(
		ModelOperationCopy modelOperationCopy
	);

	@GET
	@Path("/{id}/model_configurations")
	@LogRestClientTime
	List<ModelConfiguration> getModelConfigurations(
			@PathParam("id") String id,
			@QueryParam("page_size") int pageSize
	);
}
