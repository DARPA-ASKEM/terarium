package software.uncharted.terarium.mockdataservice.resources;

import software.uncharted.terarium.mockdataservice.models.Project;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProjectResource {

	@GET
	public List<Project> getProjects() {
		final Project pA = new Project(1L, "Project A", "Test Project A");
		final Project pB = new Project(2L, "Project B", "Test Project B");

		final List<Project> response = new ArrayList<>(2);
		response.add(pA);
		response.add(pB);

		return response;
	}

	@GET
	@Path("/{id}")
	public Project getProject(
		@QueryParam("id") final Long id
	) {
		final Project pA = new Project(1L, "Project A", "Test Project A");
		final Project pB = new Project(2L, "Project B", "Test Project B");

		if (id.equals(pA.getId())) {
			return pA;
		} else if (id.equals(pB.getId())) {
			return pB;
		}

		return null;
	}

	@POST
	public Project createProject(final Project newProject) {
		return newProject;
	}

	@PUT
	@Path("/{id}")
	public Project updateProject(final Long id, final Project updatedProject) {
		return updatedProject;
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	public Boolean deleteProject(final Long id) {
		return true;
	}
}
