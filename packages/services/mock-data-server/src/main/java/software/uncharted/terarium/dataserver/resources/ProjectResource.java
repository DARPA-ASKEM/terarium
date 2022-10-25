package software.uncharted.terarium.mockdataserver.resources;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import software.uncharted.terarium.mockdataserver.models.Project;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Path("/data/projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name="Project REST endpoints")
public class ProjectResource {

	@Inject
	Logger logger;

	@GET
	@Produces("application/json")
	public List<Project> getProjects(
		@QueryParam("sort") @DefaultValue("") String sortQuery,
		@QueryParam("page") @DefaultValue("0") int pageIndex,
		@QueryParam("size") @DefaultValue("100") int pageSize
	) {
		Project p1 = new Project("ProjectA", "This is Project A");
		Project p2 = new Project("ProjectB", "This is Project B");
		return new ArrayList<>(Arrays.asList(p1, p2));
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
