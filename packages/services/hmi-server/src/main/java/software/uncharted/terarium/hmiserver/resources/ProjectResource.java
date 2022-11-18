package software.uncharted.terarium.hmiserver.resources;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.Project;
import software.uncharted.terarium.hmiserver.proxies.ProjectProxy;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

@Path("/api/projects")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Project REST Endpoints")
public class ProjectResource {

	@Inject
	@RestClient
	ProjectProxy proxy;

	@GET
	@Tag(name = "Get all projects for a given user")
	public Response getProjects() {
		final List<Project> projects = proxy.getProjects();
		if (projects.isEmpty()) {
			return Response.noContent().build();
		}
		return Response.ok(projects).build();
	}

	@GET
	@Path("/{id}")
	public Response getProject(
		@PathParam("id") final Long id
	) {
		final Project entity = proxy.getProject(id);

		if (entity == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		return Response.ok(entity).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createProject(
		final Project newProject
	) {
		final Project entity = proxy.createProject(newProject);
		return Response.created(URI.create("/api/projects/" + entity.id)).build();
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateProject(
		@PathParam("id") final Long id,
		final Project updatedProject
	) {
		if (proxy.getProject(id) == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

		final Project entity = proxy.updateProject(id, updatedProject);

		if (entity == null) {
			return Response.noContent().build();
		}
		return Response.ok(entity).build();
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response deleteProject(
		@PathParam("id") final Long id
	) {
		if (Boolean.FALSE.equals(proxy.deleteProject(id))) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

		return Response.ok().build();
	}
}
