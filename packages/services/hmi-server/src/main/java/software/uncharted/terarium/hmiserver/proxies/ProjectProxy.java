package software.uncharted.terarium.hmiserver.proxies;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.models.Project;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@RegisterRestClient
@Path("/data/projects")
@Produces(MediaType.APPLICATION_JSON)
public interface ProjectProxy {

	@GET
	List<Project> getProjects(
		@QueryParam("sort") @DefaultValue("") String sortQuery,
		@QueryParam("page") @DefaultValue("0") int pageIndex,
		@QueryParam("size") @DefaultValue("100") int pageSize
	);

	@GET
	@Path("/{id}")
	Project getProject(
		@QueryParam("id") Long id
	);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	Project createProject(Project newProject);

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	Project updateProject(Long id, Project updatedProject);

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	Boolean deleteProject(Long id);

	@GET
	@Path("/count")
	@Produces(MediaType.TEXT_PLAIN)
	Long getNumProjects();
}
