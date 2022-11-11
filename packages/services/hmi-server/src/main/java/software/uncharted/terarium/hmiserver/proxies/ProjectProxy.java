package software.uncharted.terarium.hmiserver.proxies;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.models.Project;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@RegisterRestClient
@Path("/projects")
@Produces(MediaType.APPLICATION_JSON)
public interface ProjectProxy {

	@GET
	List<Project> getProjects();

	@GET
	@Path("/{id}")
	Project getProject(
		@QueryParam("id") Long id
	);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	Project createProject(
		Project newProject
	);

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	Project updateProject(
		Long id,
		Project updatedProject
	);

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	Boolean deleteProject(
		Long id
	);
}
