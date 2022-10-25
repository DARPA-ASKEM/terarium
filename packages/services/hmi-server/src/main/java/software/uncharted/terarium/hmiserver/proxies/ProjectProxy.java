package software.uncharted.terarium.hmiserver.proxies;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.models.Project;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@RegisterRestClient
@Path("/data/projects")
public interface ProjectProxy {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	List<Project> getProjects(
		@QueryParam("sort") @DefaultValue("") String sortQuery,
		@QueryParam("page") @DefaultValue("0") int pageIndex,
		@QueryParam("size") @DefaultValue("100") int pageSize
	);

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
