package software.uncharted.terarium.hmiserver.resources;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.Project;
import software.uncharted.terarium.hmiserver.proxies.ProjectProxy;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/api/projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Project REST Endpoint")
public class ProjectResource {

	@RestClient
	ProjectProxy proxy;

	@GET
	@Produces("application/json")
	@Tag(name = "Get all projects for a given user")
	public Response getProjects(
		@QueryParam("sort") @DefaultValue("") String sortQuery,
		@QueryParam("page") @DefaultValue("0") int pageIndex,
		@QueryParam("size") @DefaultValue("100") int pageSize
	) {
		List<Project> projects = proxy.getProjects(sortQuery, pageIndex, pageSize);
		if (projects.isEmpty()) {
			return Response.noContent().build();
		}
		return Response.ok(projects).build();
	}

//	@GET
//	@Path("/{id}")
//	public Response getProject(
//		@QueryParam("id") Long id
//	) {
//		Project entity = service.get(id);
//
//		if (entity == null) {
//			throw new WebApplicationException(Response.Status.NOT_FOUND);
//		}
//		return Response.ok(entity).build();
//	}
//
//	@POST
//	@Transactional
//	public Response createProject(Project newProject) {
//		Project entity = service.add(newProject);
//		return Response.created(URI.create("/projects/" + entity.id)).build();
//	}
//
//	@PUT
//	@Path("/{id}")
//	@Transactional
//	public Response updateProject(Long id, Project updatedProject) {
//		if (service.get(id) == null) {
//			throw new WebApplicationException(Response.Status.NOT_FOUND);
//		}
//
//		updatedProject.updatedAt = Instant.now();
//		Project entity = service.update(id, updatedProject);
//
//		if (entity == null) {
//			return Response.noContent().build();
//		}
//		return Response.ok(entity).build();
//	}
//
//	@DELETE
//	@Path("/{id}")
//	@Transactional
//	public void deleteProject(Long id) {
//		if (!service.delete(id)) {
//			throw new WebApplicationException(Response.Status.NOT_FOUND);
//		}
//	}
//
//	@GET
//	@Path("/count")
//	public Long getNumProjects() {
//		return service.count();
//	}
}
