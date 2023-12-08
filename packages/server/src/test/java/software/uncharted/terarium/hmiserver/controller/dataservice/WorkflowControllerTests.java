package software.uncharted.terarium.hmiserver.controller.dataservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.Workflow;
import software.uncharted.terarium.hmiserver.service.data.WorkflowService;

import java.io.IOException;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WorkflowControllerTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private WorkflowService workflowService;

	final Workflow workflow0 = new Workflow()
			.setId("test-workflow-id0")
			.setName("test-workflow-name0")
			.setDescription("test-workflow-description0");

	final Workflow workflow1 = new Workflow()
		.setId("test-workflow-id1")
		.setName("test-workflow-name1")
		.setDescription("test-workflow-description1");

	final Workflow updatedWorkflow0 = new Workflow()
		.setId("test-workflow-id0")
		.setName("new-test-workflow-name")
		.setDescription("new-test-workflow-description");

	@After
	public void tearDown() throws IOException {
		workflowService.deleteWorkflow(workflow0.getId());
		workflowService.deleteWorkflow(workflow1.getId());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetWorkflows() throws Exception {
		workflowService.createWorkflow(workflow0);
		workflowService.createWorkflow(workflow1);

		mockMvc.perform(MockMvcRequestBuilders.get("/workflows")
			.with(csrf()))
			.andExpect(status().isOk())
			.andReturn();
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetWorkflow() throws Exception {
		workflowService.createWorkflow(workflow0);

		mockMvc.perform(MockMvcRequestBuilders.get("/workflows/" + workflow0.getId())
			.with(csrf()))
			.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateWorkflow() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/workflows")
			.with(csrf())
			.contentType("application/json")
			.content(objectMapper.writeValueAsString(workflow0)))
			.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUpdateWorkflow() throws Exception {
		workflowService.createWorkflow(workflow0);

		mockMvc.perform(MockMvcRequestBuilders.put("/workflows" + updatedWorkflow0.getId())
			.with(csrf())
			.contentType("application/json")
			.content(objectMapper.writeValueAsString(updatedWorkflow0)))
			.andExpect(status().isOk());

		Assertions.assertEquals(workflowService.getWorkflow(updatedWorkflow0.getId()).getName(), updatedWorkflow0.getName());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteWorkflow() throws Exception {
		workflowService.createWorkflow(workflow0);

		mockMvc.perform(MockMvcRequestBuilders.delete("/workflows/" + workflow0.getId())
			.with(csrf()))
			.andExpect(status().isOk());

		Assertions.assertNull(workflowService.getWorkflow(workflow0.getId()));
	}
}
