package software.uncharted.terarium.hmiserver.controller.dataservice;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.Workflow;
import software.uncharted.terarium.hmiserver.service.data.ProjectSearchService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;
import software.uncharted.terarium.hmiserver.service.data.WorkflowService;

public class WorkflowControllerTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private WorkflowService workflowService;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private ProjectSearchService projectSearchService;

	Project project;

	@BeforeEach
	public void setup() throws IOException {
		projectSearchService.setupIndexAndAliasAndEnsureEmpty();
		project = projectService.createProject(
			(Project) new Project().setPublicAsset(true).setName("test-project-name").setDescription("my description")
		);
	}

	@AfterEach
	public void teardown() throws IOException {
		projectSearchService.teardownIndexAndAlias();
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateWorkflow() throws Exception {
		final Workflow workflow = new Workflow();
		workflow.setName("test-workflow-name0");
		workflow.setDescription("test-workflow-description");
		workflow.setPublicAsset(true);

		mockMvc
			.perform(
				MockMvcRequestBuilders.post("/workflows")
					.param("project-id", PROJECT_ID.toString())
					.with(csrf())
					.contentType("application/json")
					.content(objectMapper.writeValueAsString(workflow))
			)
			.andExpect(status().isCreated());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetWorkflow() throws Exception {
		Workflow workflow = new Workflow();
		workflow.setName("test-workflow-name1");
		workflow.setDescription("test-workflow-description");
		workflow = workflowService.createAsset(workflow, project.getId());

		mockMvc
			.perform(
				MockMvcRequestBuilders.get("/workflows/" + workflow.getId())
					.param("project-id", PROJECT_ID.toString())
					.with(csrf())
			)
			.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUpdateWorkflow() throws Exception {
		Workflow workflow = new Workflow();
		workflow.setName("test-workflow-name1");
		workflow.setDescription("test-workflow-description");
		workflow = workflowService.createAsset(workflow, project.getId());

		mockMvc
			.perform(
				MockMvcRequestBuilders.put("/workflows/" + workflow.getId())
					.param("project-id", PROJECT_ID.toString())
					.with(csrf())
					.contentType("application/json")
					.content(objectMapper.writeValueAsString(workflow))
			)
			.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteWorkflow() throws Exception {
		Workflow workflow = new Workflow();
		workflow.setName("test-workflow-name1");
		workflow.setDescription("test-workflow-description");
		workflow = workflowService.createAsset(workflow, project.getId());

		mockMvc
			.perform(
				MockMvcRequestBuilders.delete("/workflows/" + workflow.getId())
					.param("project-id", PROJECT_ID.toString())
					.with(csrf())
			)
			.andExpect(status().isOk());

		Assertions.assertTrue(workflowService.getAsset(workflow.getId()).isEmpty());
	}
}
