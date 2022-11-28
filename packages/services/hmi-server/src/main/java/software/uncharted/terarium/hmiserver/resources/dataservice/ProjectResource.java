package software.uncharted.terarium.hmiserver.resources.dataservice;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Project;
import software.uncharted.terarium.hmiserver.models.dataservice.ResourceType;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ProjectProxy;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/projects")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Project REST Endpoints")
public class ProjectResource {

	@Inject
	@RestClient
	ProjectProxy proxy;

	@GET
	public Response getProjects(
		@QueryParam("page_size") final Integer pageSize,
		@QueryParam("page") final Integer page
	) {
		return proxy.getProjects(pageSize, page);
	}

	@GET
	@Path("/{id}")
	public Response getProject(
		@PathParam("id") final String id
	) {
		return proxy.getProject(id);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createProject(
		final Project newProject
	) {
		return proxy.createProject(newProject);
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateProject(
		@PathParam("id") final String id,
		final Project updatedProject
	) {
		return proxy.updateProject(id, updatedProject);
	}

	@GET
	@Path("/{project_id}/assets/{resource_type}/{resource_id}")
	public Response getAsset(
		@PathParam("project_id") final String projectId,
		@PathParam("resource_type") final ResourceType type,
		@PathParam("resource_id") final String resourceId
	) {
		return proxy.getAsset(projectId, type, resourceId);
	}

	@POST
	@Path("/{project_id}/assets/{resource_type}/{resource_id}")
	public Response createAsset(
		@PathParam("project_id") final String projectId,
		@PathParam("resource_type") final String type, // ResourceType
		@PathParam("resource_id") final String resourceId
	) {
		return proxy.createAsset(projectId, type, resourceId);
	}
}
