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
import software.uncharted.terarium.hmiserver.service.data.ProjectService;
import software.uncharted.terarium.hmiserver.service.data.WorkflowService;

public class WorkflowControllerTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private WorkflowService workflowService;

	@Autowired
	private ProjectService projectService;

	Project project;

	@BeforeEach
	public void setup() throws IOException {
		workflowService.setupIndexAndAliasAndEnsureEmpty();

		project = projectService.createProject(
			(Project) new Project().setPublicAsset(true).setName("test-project-name").setDescription("my description")
		);
	}

	@AfterEach
	public void teardown() throws IOException {
		workflowService.teardownIndexAndAlias();
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
	public void testItCanGetWorkflows() throws Exception {
		final Workflow workflow = new Workflow();
		workflow.setName("test-workflow-name1");
		workflow.setDescription("test-workflow-description");
		workflow.setPublicAsset(true);

		final Workflow workflow2 = new Workflow();
		workflow2.setName("test-workflow-name2");
		workflow2.setDescription("test-workflow-description2");
		workflow2.setPublicAsset(true);

		final Workflow workflow3 = new Workflow();
		workflow3.setName("test-workflow-name3");
		workflow3.setDescription("test-workflow-description3");
		workflow3.setPublicAsset(true);

		workflowService.createAsset(workflow, project.getId(), ASSUME_WRITE_PERMISSION);
		workflowService.createAsset(workflow2, project.getId(), ASSUME_WRITE_PERMISSION);
		workflowService.createAsset(workflow3, project.getId(), ASSUME_WRITE_PERMISSION);

		mockMvc.perform(MockMvcRequestBuilders.get("/workflows").with(csrf())).andExpect(status().isOk()).andReturn();
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetWorkflow() throws Exception {
		Workflow workflow = new Workflow();
		workflow.setName("test-workflow-name1");
		workflow.setDescription("test-workflow-description");
		workflow = workflowService.createAsset(workflow, project.getId(), ASSUME_WRITE_PERMISSION);

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
		workflow = workflowService.createAsset(workflow, project.getId(), ASSUME_WRITE_PERMISSION);

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
		workflow = workflowService.createAsset(workflow, project.getId(), ASSUME_WRITE_PERMISSION);

		mockMvc
			.perform(
				MockMvcRequestBuilders.delete("/workflows/" + workflow.getId())
					.param("project-id", PROJECT_ID.toString())
					.with(csrf())
			)
			.andExpect(status().isOk());

		Assertions.assertTrue(workflowService.getAsset(workflow.getId(), ASSUME_WRITE_PERMISSION).isEmpty());
	}
}
