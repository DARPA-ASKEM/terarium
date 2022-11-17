package software.uncharted.terarium.hmiserver.proxies;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.models.Model;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@RegisterRestClient(configKey = "data-service")
@Path("/models")
@Produces(MediaType.APPLICATION_JSON)
public interface ModelProxy {

	@GET
	List<Model> getModels();

	@GET
	@Path("/{id}")
	Model getModel(
		@PathParam("id") Long id
	);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	Model createModel(
		Model model
	);

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	Model updateModel(
		@PathParam("id") Long id,
		Model plan
	);

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	Boolean deleteModel(
		@PathParam("id") Long id
	);
}
