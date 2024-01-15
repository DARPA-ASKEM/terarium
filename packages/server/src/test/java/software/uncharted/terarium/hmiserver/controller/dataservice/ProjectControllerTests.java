package software.uncharted.terarium.hmiserver.controller.dataservice;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;

@Transactional
public class ProjectControllerTests extends TerariumApplicationTests {
	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProjectService projectService;

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateProject() throws Exception {

		final Project project = new Project()
				.setName("test-name");

		mockMvc.perform(MockMvcRequestBuilders.post("/projects")
				.with(csrf())
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(project)))
				.andExpect(status().isCreated());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetProject() throws Exception {

		final Project project = projectService.createProject(new Project()
				.setName("test-name"));

		mockMvc.perform(MockMvcRequestBuilders.get("/projects/" + project.getId())
				.with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUpdateProject() throws Exception {

		final Project project = projectService.createProject(new Project()
				.setName("test-name"));

		mockMvc.perform(MockMvcRequestBuilders.put("/projects/" + project.getId())
				.with(csrf())
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(project)))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteProject() throws Exception {

		final Project project = projectService.createProject(new Project()
				.setName("test-name"));

		mockMvc.perform(MockMvcRequestBuilders.delete("/projects/" + project.getId())
				.with(csrf()))
				.andExpect(status().isOk());

		Assertions.assertTrue(projectService.getProject(project.getId()).isEmpty());
	}

}
