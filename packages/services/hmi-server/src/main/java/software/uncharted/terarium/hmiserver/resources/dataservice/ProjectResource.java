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
		@DefaultValue("50") @QueryParam("page_size") final Integer pageSize,
		@DefaultValue("0") @QueryParam("page") final Integer page
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
		final Project project
	) {
		return proxy.createProject(project);
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateProject(
		@PathParam("id") final String id,
		final Project project
	) {
		return proxy.updateProject(id, project);
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response deleteProject(
		@PathParam("id") final String id
	) {
		return proxy.deleteProject(id);
	}

	@GET
	@Path("/{project_id}/assets")
	public Response getAssets(
		@PathParam("project_id") final String projectId
	) {
		return proxy.getAssets(projectId);
	}

	@POST
	@Path("/{project_id}/assets/{resource_type}/{resource_id}")
	public Response createAsset(
		@PathParam("project_id") final String projectId,
		@PathParam("resource_type") final String type, // ResourceType
		@PathParam("resource_id") final String resourceId
	) {
		return proxy.createAsset(projectId, ResourceType.findByType(type).name(), resourceId);
	}

	@DELETE
	@Path("/{project_id}/assets/{resource_type}/{resource_id}")
	public Response deleteAsset(
		@PathParam("project_id") final String projectId,
		@PathParam("resource_type") final String type, // ResourceType
		@PathParam("resource_id") final String resourceId
	) {
		return proxy.deleteAsset(projectId, ResourceType.findByType(type).name(), resourceId);
	}
}
