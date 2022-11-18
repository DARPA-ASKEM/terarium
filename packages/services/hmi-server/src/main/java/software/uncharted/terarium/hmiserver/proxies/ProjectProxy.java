package software.uncharted.terarium.hmiserver.proxies;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Project;
import software.uncharted.terarium.hmiserver.models.dataservice.ResourceType;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;


@RegisterRestClient(configKey = "data-service")
@Path("/projects")
@Produces(MediaType.APPLICATION_JSON)
public interface ProjectProxy {

	@GET
	Response getProjects(
		@QueryParam("page_size") Integer pageSize,
		@QueryParam("page") Integer page
	);

	@GET
	@Path("/{id}")
	Response getProject(
		@PathParam("id") String id
	);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	Response createProject(
		Project newProject
	);

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	Response updateProject(
		@PathParam("id") String id,
		Project updatedProject
	);

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	Response deleteProject(
		@PathParam("id") String id
	);

	@GET
	@Path("/{project_id}/assets/{resource_type}/{resource_id}")
	Response getAsset(
		@PathParam("project_id") String projectId,
		@PathParam("resource_type") ResourceType type,
		@PathParam("resource_id") String resourceId
	);

	@POST
	@Path("/{project_id}/assets/{resource_type}/{resource_id}")
	@Consumes(MediaType.APPLICATION_JSON)
	Response createAsset(
		@PathParam("project_id") String projectId,
		@PathParam("resource_type") ResourceType type,
		@PathParam("resource_id") String resourceId,
		List<String> asset
	);
}
