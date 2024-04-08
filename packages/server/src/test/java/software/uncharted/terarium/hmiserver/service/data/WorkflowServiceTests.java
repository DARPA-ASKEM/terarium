package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.Workflow;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.WorkflowEdge;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.WorkflowNode;

@Slf4j
public class WorkflowServiceTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private WorkflowService workflowService;

	@BeforeEach
	public void setup() throws IOException {
		workflowService.setupIndexAndAliasAndEnsureEmpty();
	}

	@AfterEach
	public void teardown() throws IOException {
		workflowService.teardownIndexAndAlias();
	}

	Workflow createWorkflow() throws Exception {
		return (Workflow) new Workflow()
				.setName("test-workflow-name-0")
				.setDescription("test-workflow-description-0")
				.setPublicAsset(true);
	}

	Workflow createWorkflow(final String key) throws Exception {
		return (Workflow) new Workflow()
				.setName("test-workflow-name-" + key)
				.setDescription("test-workflow-description-" + key)
				.setPublicAsset(true);
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateWorkflow() throws Exception {

		final Workflow workflow = workflowService.createAsset(createWorkflow());

		Assertions.assertNotNull(workflow.getId());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetWorkflows() throws Exception {

		workflowService.createAsset(createWorkflow("0"));
		workflowService.createAsset(createWorkflow("1"));
		workflowService.createAsset(createWorkflow("2"));

		final List<Workflow> workflows = workflowService.getAssets(0, 100);

		Assertions.assertEquals(3, workflows.size());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetWorkflow() throws Exception {

		final Workflow workflow = workflowService.createAsset(createWorkflow());

		final Workflow fetchedWorkflow = workflowService.getAsset(workflow.getId()).get();

		Assertions.assertEquals(workflow, fetchedWorkflow);
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUpdateWorkflow() throws Exception {

		final Workflow workflow = workflowService.createAsset(createWorkflow());
		workflow.setName("new name");

		final Workflow updatedWorkflow = workflowService.updateAsset(workflow);

		Assertions.assertEquals(workflow, updatedWorkflow);
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteWorkflow() throws Exception {

		final Workflow workflow = workflowService.createAsset(createWorkflow());

		workflowService.deleteAsset(workflow.getId());

		final Optional<Workflow> deleted = workflowService.getAsset(workflow.getId());

		Assertions.assertTrue(deleted.isEmpty());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCloneWorkflow() throws Exception {

		final WorkflowNode a = new WorkflowNode().setId(UUID.randomUUID());
		final WorkflowNode b = new WorkflowNode().setId(UUID.randomUUID());
		final WorkflowNode c = new WorkflowNode().setId(UUID.randomUUID());
		final WorkflowNode d = new WorkflowNode().setId(UUID.randomUUID());

		final WorkflowEdge ab = new WorkflowEdge().setSource(a).setTarget(b);
		final WorkflowEdge bc = new WorkflowEdge().setSource(b).setTarget(c);
		final WorkflowEdge cd = new WorkflowEdge().setSource(c).setTarget(d);

		Workflow workflow = createWorkflow();
		workflow.setNodes(List.of(a, b, c, d)).setEdges(List.of(ab, bc, cd));

		workflow = workflowService.createAsset(workflow);

		final Workflow cloned = workflowService.cloneAsset(workflow.getId());

		Assertions.assertNotEquals(workflow.getId(), cloned.getId());
		Assertions.assertEquals(workflow.getNodes().size(), cloned.getNodes().size());
		Assertions.assertNotEquals(workflow.getNodes().get(0).getId(), cloned.getNodes().get(0).getId());
		Assertions.assertNotEquals(workflow.getNodes().get(1).getId(), cloned.getNodes().get(1).getId());
		Assertions.assertNotEquals(workflow.getNodes().get(2).getId(), cloned.getNodes().get(2).getId());
		Assertions.assertNotEquals(workflow.getNodes().get(3).getId(), cloned.getNodes().get(3).getId());
		Assertions.assertEquals(workflow.getEdges().size(), cloned.getEdges().size());
		Assertions.assertNotEquals(workflow.getEdges().get(0).getId(), cloned.getEdges().get(0).getId());
		Assertions.assertNotEquals(workflow.getEdges().get(1).getId(), cloned.getEdges().get(1).getId());
		Assertions.assertNotEquals(workflow.getEdges().get(2).getId(), cloned.getEdges().get(2).getId());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testWorkflowsAreOpaque() throws Exception {

		final WorkflowNode a = mapper.readValue("{\"id\":\"" + UUID.randomUUID() + "\", \"otherField\": 123 }",
				WorkflowNode.class);

		final WorkflowNode b = mapper.readValue(
				"{\"id\":\"" + UUID.randomUUID() + "\", \"anotherField\": \"text value\" }",
				WorkflowNode.class);

		final WorkflowEdge e = mapper.readValue(
				"{\"id\":\"" + UUID.randomUUID() + "\""
						+ ", \"source\": " + mapper.writeValueAsString(a)
						+ ", \"target\": " + mapper.writeValueAsString(b)
						+ ", \"somethingElse\": \"some value\"}",
				WorkflowEdge.class);

		Workflow workflow = new Workflow().setNodes(List.of(a, b)).setEdges(List.of(e));
		workflow.setPublicAsset(true);

		workflow = workflowService.createAsset(workflow);

		JsonNode raw = mapper.valueToTree(workflow);
		raw.get("nodes").forEach(n -> {
			Assertions.assertTrue(n.has("otherField") || n.has("anotherField"));
		});
		raw.get("edges").forEach(n -> {
			Assertions.assertTrue(n.has("somethingElse"));
		});

		final Workflow cloned = workflowService.cloneAsset(workflow.getId());

		Assertions.assertNotEquals(workflow.getId(), cloned.getId());
		Assertions.assertEquals(workflow.getNodes().size(), cloned.getNodes().size());
		Assertions.assertNotEquals(workflow.getNodes().get(0).getId(), cloned.getNodes().get(0).getId());
		Assertions.assertNotEquals(workflow.getNodes().get(1).getId(), cloned.getNodes().get(1).getId());
		Assertions.assertEquals(workflow.getEdges().size(), cloned.getEdges().size());
		Assertions.assertNotEquals(workflow.getEdges().get(0).getId(), cloned.getEdges().get(0).getId());

		// ensure additional fields are preserved on clone
		raw = mapper.valueToTree(workflow);
		raw.get("nodes").forEach(n -> {
			Assertions.assertTrue(n.has("otherField") || n.has("anotherField"));
		});
		raw.get("edges").forEach(n -> {
			Assertions.assertTrue(n.has("somethingElse"));
		});
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanSyncToNewIndex() throws Exception {

		final int NUM = 32;

		for (int i = 0; i < NUM; i++) {
			workflowService.createAsset(createWorkflow(String.valueOf(i)));
		}

		final String currentIndex = workflowService.getCurrentAssetIndex();

		Assertions.assertEquals(NUM, workflowService.getAssets(0, NUM).size());

		workflowService.syncAllAssetsToNewIndex(true);

		final String newIndex = workflowService.getCurrentAssetIndex();

		Assertions.assertEquals(NUM, workflowService.getAssets(0, NUM).size());

		Assertions.assertNotEquals(currentIndex, newIndex);
	}
}
