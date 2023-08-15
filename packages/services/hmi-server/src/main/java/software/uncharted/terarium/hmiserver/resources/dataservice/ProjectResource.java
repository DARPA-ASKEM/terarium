package software.uncharted.terarium.hmiserver.resources.dataservice;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Assets;
import software.uncharted.terarium.hmiserver.models.dataservice.Project;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ProjectProxy;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("/api/projects")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Project REST Endpoints")
public class ProjectResource {

	@Inject
	@RestClient
	ProjectProxy proxy;

	@GET
	public Response getProjects(
		@DefaultValue("250") @QueryParam("page_size") final Integer pageSize,
		@DefaultValue("0") @QueryParam("page") final Integer page
	) {
		List<Project> projects = proxy.getProjects(pageSize, page);

		// Remove non-active (soft-deleted) projects
		projects = projects
			.stream()
			.filter(Project::getActive)
			.toList();

		projects.forEach(project -> {
			Assets assets = proxy.getAssets(project.getProjectID(), Arrays.asList("datasets", "models", "publications"));
			Map<String, String> metadata = new HashMap<>();
			metadata.put("datasets-count", assets.getDatasets() == null ? "0" : String.valueOf(assets.getDatasets().size()));
			metadata.put("extractions-count", assets.getExtractions() == null ? "0" : String.valueOf(assets.getExtractions().size()));
			metadata.put("models-count", assets.getModels() == null ? "0" : String.valueOf(assets.getModels().size()));
			metadata.put("publications-count", assets.getPublications() == null ? "0" : String.valueOf(assets.getPublications().size()));
			metadata.put("workflows-count", assets.getWorkflows() == null ? "0" : String.valueOf(assets.getWorkflows().size()));
			metadata.put("artifacts-count", assets.getArtifacts() == null ? "0" : String.valueOf(assets.getArtifacts().size()));
			project.setMetadata(metadata);
		});

		return Response
			.status(Response.Status.OK)
			.entity(projects)
			.build();
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
		@PathParam("project_id") final String projectId,
		@QueryParam("types") final List<String> types
	) {
		return Response
			.status(Response.Status.OK)
			.entity(proxy.getAssets(projectId, types))
			.type(MediaType.APPLICATION_JSON)
			.build();

	}

	@POST
	@Path("/{project_id}/assets/{resource_type}/{resource_id}")
	public Response createAsset(
		@PathParam("project_id") final String projectId,
		@PathParam("resource_type") final String type, // ResourceType
		@PathParam("resource_id") final String resourceId
	) {
		return proxy.createAsset(projectId, type, resourceId); // ResourceType.findByType(type).name()
	}

	@DELETE
	@Path("/{project_id}/assets/{resource_type}/{resource_id}")
	public Response deleteAsset(
		@PathParam("project_id") final String projectId,
		@PathParam("resource_type") final String type, // ResourceType
		@PathParam("resource_id") final String resourceId
	) {
		return proxy.deleteAsset(projectId, type, resourceId);
	}
}
