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
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.Workflow;
import software.uncharted.terarium.hmiserver.service.data.WorkflowService;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

public class WorkflowControllerTests extends TerariumApplicationTests {

  @Autowired private ObjectMapper objectMapper;

  @Autowired private WorkflowService workflowService;

  @Autowired private ElasticsearchService elasticService;

  @Autowired private ElasticsearchConfiguration elasticConfig;

  @BeforeEach
  public void setup() throws IOException {
    elasticService.createOrEnsureIndexIsEmpty(elasticConfig.getWorkflowIndex());
  }

  @AfterEach
  public void teardown() throws IOException {
    elasticService.deleteIndex(elasticConfig.getWorkflowIndex());
  }

  final Workflow workflow =
      new Workflow().setName("test-workflow-name0").setDescription("test-workflow-description");

  @Test
  @WithUserDetails(MockUser.URSULA)
  public void testItCanCreateWorkflow() throws Exception {

    final Workflow workflow =
        new Workflow().setName("test-workflow-name0").setDescription("test-workflow-description");

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/workflows")
                .with(csrf())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(workflow)))
        .andExpect(status().isCreated());
  }

  @Test
  @WithUserDetails(MockUser.URSULA)
  public void testItCanGetWorkflows() throws Exception {

    workflowService.createAsset(
        new Workflow().setName("test-workflow-name0").setDescription("test-workflow-description"));
    workflowService.createAsset(
        new Workflow().setName("test-workflow-name0").setDescription("test-workflow-description"));
    workflowService.createAsset(
        new Workflow().setName("test-workflow-name0").setDescription("test-workflow-description"));

    mockMvc
        .perform(MockMvcRequestBuilders.get("/workflows").with(csrf()))
        .andExpect(status().isOk())
        .andReturn();
  }

  @Test
  @WithUserDetails(MockUser.URSULA)
  public void testItCanGetWorkflow() throws Exception {

    final Workflow workflow =
        workflowService.createAsset(
            new Workflow()
                .setName("test-workflow-name0")
                .setDescription("test-workflow-description"));

    mockMvc
        .perform(MockMvcRequestBuilders.get("/workflows/" + workflow.getId()).with(csrf()))
        .andExpect(status().isOk());
  }

  @Test
  @WithUserDetails(MockUser.URSULA)
  public void testItCanUpdateWorkflow() throws Exception {

    final Workflow workflow =
        workflowService.createAsset(
            new Workflow()
                .setName("test-workflow-name0")
                .setDescription("test-workflow-description"));

    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/workflows/" + workflow.getId())
                .with(csrf())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(workflow)))
        .andExpect(status().isOk());
  }

  @Test
  @WithUserDetails(MockUser.URSULA)
  public void testItCanDeleteWorkflow() throws Exception {

    final Workflow workflow =
        workflowService.createAsset(
            new Workflow()
                .setName("test-workflow-name0")
                .setDescription("test-workflow-description"));

    mockMvc
        .perform(MockMvcRequestBuilders.delete("/workflows/" + workflow.getId()).with(csrf()))
        .andExpect(status().isOk());

    Assertions.assertTrue(workflowService.getAsset(workflow.getId()).isEmpty());
  }
}
