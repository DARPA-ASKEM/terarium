package software.uncharted.terarium.mockdataservice.resources;

import software.uncharted.terarium.mockdataservice.models.Project;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProjectResource {

	private final Map<Long, Project> projects;

	public ProjectResource() {
		this.projects = new HashMap<>();
		this.projects.put(1L, new Project(
			1L,
			"Project A",
			"Test Project A",
			Map.of("asset1", List.of(1L, 2L), "asset2", List.of(3L, 4L)),
			"active"
		));
		this.projects.put(2L, new Project(
			2L,
			"Project B",
			"Test Project B",
			Map.of("asset1", List.of(5L, 6L), "asset2", List.of(7L, 8L)),
			"active"
		));
	}

	@GET
	public Collection<Project> getProjects() {
		return this.projects.values();
	}

	@GET
	@Path("/{id}")
	public Project getProject(
		@PathParam("id") final Long id
	) {
		return this.projects.getOrDefault(id, null);
	}

	@POST
	public Project createProject(
		final Project newProject
	) {
		if (this.projects.containsKey(newProject.getId())) return null;
		this.projects.put(newProject.getId(), newProject);
		return newProject;
	}

	@PUT
	@Path("/{id}")
	public Project updateProject(
		@PathParam("id") final Long id,
		final Project updatedProject
	) {
		if (this.projects.containsKey(updatedProject.getId())) {
			this.projects.replace(updatedProject.getId(), updatedProject);
			return updatedProject;
		}
		return null;
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	public Boolean deleteProject(
		@PathParam("id") final Long id
	) {
		if (this.projects.containsKey(id)) {
			this.projects.remove(id);
			return true;
		}
		return false;
	}
}
