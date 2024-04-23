package software.uncharted.terarium.hmiserver.service.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.ProgressState;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.Simulation;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.SimulationEngine;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.SimulationType;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.Transform;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.Workflow;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.WorkflowEdge;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.WorkflowNode;
import software.uncharted.terarium.hmiserver.models.simulationservice.SimulationRequest;
import software.uncharted.terarium.hmiserver.models.simulationservice.parts.TimeSpan;

@Slf4j
public class WorkflowServiceTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private WorkflowService workflowService;

	@Autowired
	SimulationService simulationService;

	static ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	public void setup() throws IOException {
		workflowService.setupIndexAndAliasAndEnsureEmpty();
	}

	@AfterEach
	public void teardown() throws IOException {
		workflowService.teardownIndexAndAlias();
	}

	static SimulationRequest createSimRequest() {
		final SimulationRequest request = new SimulationRequest();
		request.setModelConfigId(UUID.randomUUID());
		request.setTimespan(new TimeSpan());
		return request;
	}

	static Simulation createSimulation(final String key) {
		final Simulation simulation = new Simulation();
		simulation.setName("test-simulation-name-" + key);
		simulation.setDescription("test-simulation-description-" + key);
		simulation.setType(SimulationType.SIMULATION);
		simulation.setExecutionPayload(objectMapper.convertValue(createSimRequest(), JsonNode.class));
		simulation.setResultFiles(Arrays.asList("never", "gonna", "give", "you", "up"));
		simulation.setStatus(ProgressState.RUNNING);
		simulation.setEngine(SimulationEngine.SCIML);

		return simulation;
	}

	static Workflow createWorkflow() throws Exception {

		final WorkflowNode a = new WorkflowNode().setId(UUID.randomUUID());
		final WorkflowNode b = new WorkflowNode().setId(UUID.randomUUID());
		final WorkflowNode c = new WorkflowNode().setId(UUID.randomUUID());
		final WorkflowNode d = new WorkflowNode().setId(UUID.randomUUID());

		final WorkflowEdge ab = new WorkflowEdge().setSource(a.getId()).setTarget(b.getId());
		final WorkflowEdge bc = new WorkflowEdge().setSource(b.getId()).setTarget(c.getId());
		final WorkflowEdge cd = new WorkflowEdge().setSource(c.getId()).setTarget(d.getId());

		return (Workflow) new Workflow()
				.setDescription("test-workflow-description-0")
				.setTransform(new Transform().setX(1).setY(2).setK(3))
				.setNodes(List.of(a, b, c, d))
				.setEdges(List.of(ab, bc, cd))
				.setPublicAsset(true)
				.setName("test-workflow-name-0");
	}

	static Workflow createWorkflow(final String key) throws Exception {

		final WorkflowNode a = new WorkflowNode().setId(UUID.randomUUID());
		final WorkflowNode b = new WorkflowNode().setId(UUID.randomUUID());
		final WorkflowNode c = new WorkflowNode().setId(UUID.randomUUID());
		final WorkflowNode d = new WorkflowNode().setId(UUID.randomUUID());

		final WorkflowEdge ab = new WorkflowEdge().setSource(a.getId()).setTarget(b.getId());
		final WorkflowEdge bc = new WorkflowEdge().setSource(b.getId()).setTarget(c.getId());
		final WorkflowEdge cd = new WorkflowEdge().setSource(c.getId()).setTarget(d.getId());

		final Workflow workflow = new Workflow();
		workflow.setName("test-workflow-name-" + key);
		workflow.setDescription("test-workflow-description-" + key);
		workflow.setTransform(new Transform().setX(1).setY(2).setK(3));
		workflow.setNodes(List.of(a, b, c, d));
		workflow.setEdges(List.of(ab, bc, cd));
		workflow.setPublicAsset(true);

		return workflow;
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateWorkflow() throws Exception {

		final Workflow before = (Workflow) createWorkflow().setId(UUID.randomUUID());
		final Workflow after = workflowService.createAsset(before);

		Assertions.assertEquals(before.getId(), after.getId());
		Assertions.assertNotNull(after.getId());
		Assertions.assertNotNull(after.getCreatedOn());
		Assertions.assertEquals(after.getNodes().size(), 4);
		for (final WorkflowNode node : after.getNodes()) {
			Assertions.assertEquals(after.getId(), node.getWorkflowId());
		}
		Assertions.assertEquals(after.getEdges().size(), 3);
		for (final WorkflowEdge edge : after.getEdges()) {
			Assertions.assertEquals(after.getId(), edge.getWorkflowId());
		}
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCantCreateDuplicates() throws Exception {

		final Workflow workflow = (Workflow) createWorkflow().setId(UUID.randomUUID());

		workflowService.createAsset(workflow);

		try {
			workflowService.createAsset(workflow);
			Assertions.fail("Should have thrown an exception");
		} catch (final IllegalArgumentException e) {
			Assertions.assertTrue(e.getMessage().contains("already exists"));
		}
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetWorkflows() throws Exception {

		workflowService.createAsset(createWorkflow("0"));
		workflowService.createAsset(createWorkflow("1"));
		workflowService.createAsset(createWorkflow("2"));

		final List<Workflow> workflows = workflowService.getAssets(0, 3);

		Assertions.assertEquals(3, workflows.size());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetWorkflow() throws Exception {

		final Workflow workflow = workflowService.createAsset(createWorkflow());

		final Workflow fetchedWorkflow =
				workflowService.getAsset(workflow.getId()).get();

		Assertions.assertEquals(workflow, fetchedWorkflow);
		Assertions.assertEquals(workflow.getId(), fetchedWorkflow.getId());
		Assertions.assertEquals(workflow.getCreatedOn(), fetchedWorkflow.getCreatedOn());
		Assertions.assertEquals(workflow.getUpdatedOn(), fetchedWorkflow.getUpdatedOn());
		Assertions.assertEquals(workflow.getDeletedOn(), fetchedWorkflow.getDeletedOn());
		Assertions.assertEquals(workflow.getTransform(), fetchedWorkflow.getTransform());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUpdateWorkflow() throws Exception {

		final Workflow workflow = workflowService.createAsset(createWorkflow());
		workflow.setName("new name");

		final Workflow updatedWorkflow = workflowService.updateAsset(workflow).orElseThrow();

		Assertions.assertEquals(workflow, updatedWorkflow);
		Assertions.assertNotNull(updatedWorkflow.getUpdatedOn());
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

		Simulation simulation = createSimulation("A");
		simulation = simulationService.createAsset(simulation);

		Workflow workflow = createWorkflow();
		workflow = workflowService.createAsset(workflow);

		workflow.setSimulation(simulation);
		workflowService.updateAsset(workflow);

		final Workflow cloned = workflowService.cloneAsset(workflow.getId());

		Assertions.assertNotEquals(workflow.getId(), cloned.getId());
		Assertions.assertEquals(workflow.getNodes().size(), cloned.getNodes().size());
		Assertions.assertNotEquals(
				workflow.getNodes().get(0).getId(), cloned.getNodes().get(0).getId());
		Assertions.assertNotEquals(
				workflow.getNodes().get(0).getWorkflowId(),
				cloned.getNodes().get(0).getWorkflowId());
		Assertions.assertNotEquals(
				workflow.getNodes().get(1).getId(), cloned.getNodes().get(1).getId());
		Assertions.assertNotEquals(
				workflow.getNodes().get(1).getWorkflowId(),
				cloned.getNodes().get(1).getWorkflowId());
		Assertions.assertNotEquals(
				workflow.getNodes().get(2).getId(), cloned.getNodes().get(2).getId());
		Assertions.assertNotEquals(
				workflow.getNodes().get(2).getWorkflowId(),
				cloned.getNodes().get(2).getWorkflowId());
		Assertions.assertNotEquals(
				workflow.getNodes().get(3).getId(), cloned.getNodes().get(3).getId());
		Assertions.assertNotEquals(
				workflow.getNodes().get(3).getWorkflowId(),
				cloned.getNodes().get(3).getWorkflowId());
		Assertions.assertEquals(workflow.getEdges().size(), cloned.getEdges().size());
		Assertions.assertNotEquals(
				workflow.getEdges().get(0).getId(), cloned.getEdges().get(0).getId());
		Assertions.assertNotEquals(
				workflow.getEdges().get(0).getWorkflowId(),
				cloned.getEdges().get(0).getWorkflowId());
		Assertions.assertNotEquals(
				workflow.getEdges().get(1).getId(), cloned.getEdges().get(1).getId());
		Assertions.assertNotEquals(
				workflow.getEdges().get(1).getWorkflowId(),
				cloned.getEdges().get(1).getWorkflowId());
		Assertions.assertNotEquals(
				workflow.getEdges().get(2).getId(), cloned.getEdges().get(2).getId());
		Assertions.assertNotEquals(
				workflow.getEdges().get(2).getWorkflowId(),
				cloned.getEdges().get(2).getWorkflowId());
		Assertions.assertNotEquals(
				workflow.getSimulation().getId(), cloned.getSimulation().getId());
		Assertions.assertEquals(
				workflow.getSimulation().getName(), cloned.getSimulation().getName());
		Assertions.assertEquals(
				workflow.getSimulation().getDescription(),
				cloned.getSimulation().getDescription());
		Assertions.assertEquals(
				workflow.getSimulation().getResultFiles().size(),
				cloned.getSimulation().getResultFiles().size());
		Assertions.assertEquals(
				workflow.getSimulation().getExecutionPayload(),
				cloned.getSimulation().getExecutionPayload());
		Assertions.assertEquals(
				workflow.getSimulation().getType(), cloned.getSimulation().getType());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanExportAndImportWorkflow() throws Exception {

		Workflow workflow = createWorkflow();
		workflow = workflowService.createAsset(workflow);

		// TODO: Once we have import/export nailed down we need to test simulations here too

		final byte[] exported = workflowService.exportAsset(workflow.getId());

		final Workflow imported = workflowService.importAsset(exported);

		Assertions.assertNotEquals(workflow.getId(), imported.getId());
		Assertions.assertEquals(workflow.getName(), imported.getName());
		Assertions.assertEquals(workflow.getDescription(), imported.getDescription());
		Assertions.assertEquals(workflow.getTransform(), imported.getTransform());
		Assertions.assertEquals(workflow.getNodes().size(), imported.getNodes().size());
		Assertions.assertNotEquals(
				workflow.getNodes().get(0).getId(), imported.getNodes().get(0).getId());
		Assertions.assertNotEquals(
				workflow.getNodes().get(1).getId(), imported.getNodes().get(1).getId());
		Assertions.assertNotEquals(
				workflow.getNodes().get(2).getId(), imported.getNodes().get(2).getId());
		Assertions.assertNotEquals(
				workflow.getNodes().get(3).getId(), imported.getNodes().get(3).getId());
		Assertions.assertEquals(workflow.getEdges().size(), imported.getEdges().size());
		Assertions.assertNotEquals(
				workflow.getEdges().get(0).getId(), imported.getEdges().get(0).getId());
		Assertions.assertNotEquals(
				workflow.getEdges().get(1).getId(), imported.getEdges().get(1).getId());
		Assertions.assertNotEquals(
				workflow.getEdges().get(2).getId(), imported.getEdges().get(2).getId());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testWorkflowsAreOpaque() throws Exception {

		final WorkflowNode a =
				mapper.readValue("{\"id\":\"" + UUID.randomUUID() + "\", \"otherField\": 123 }", WorkflowNode.class);

		final WorkflowNode b = mapper.readValue(
				"{\"id\":\"" + UUID.randomUUID() + "\", \"anotherField\": \"text value\" }", WorkflowNode.class);

		final WorkflowEdge e = mapper.readValue(
				"{\"id\":\"" + UUID.randomUUID() + "\""
						+ ", \"source\": \"" + a.getId() + "\""
						+ ", \"target\": \"" + b.getId() + "\""
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
		Assertions.assertNotEquals(
				workflow.getNodes().get(0).getId(), cloned.getNodes().get(0).getId());
		Assertions.assertNotEquals(
				workflow.getNodes().get(1).getId(), cloned.getNodes().get(1).getId());
		Assertions.assertEquals(workflow.getEdges().size(), cloned.getEdges().size());
		Assertions.assertNotEquals(
				workflow.getEdges().get(0).getId(), cloned.getEdges().get(0).getId());

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
	public void testItCanSearchAssets() throws Exception {

		final int NUM = 32;

		List<Workflow> workflows = new ArrayList<>();
		for (int i = 0; i < NUM; i++) {
			workflows.add(createWorkflow(String.valueOf(i)));
		}
		workflows = workflowService.createAssets(workflows);

		final List<Workflow> results = workflowService.searchAssets(0, NUM, null);

		Assertions.assertEquals(NUM, results.size());

		for (int i = 0; i < results.size(); i++) {
			Assertions.assertEquals(workflows.get(i).getName(), results.get(i).getName());
			Assertions.assertEquals(
					workflows.get(i).getDescription(), results.get(i).getDescription());
			Assertions.assertEquals(
					workflows.get(i).getTransform(), results.get(i).getTransform());
			Assertions.assertEquals(
					workflows.get(i).getCreatedOn().toInstant().getEpochSecond(),
					results.get(i).getCreatedOn().toInstant().getEpochSecond());
			Assertions.assertEquals(
					workflows.get(i).getUpdatedOn(), results.get(i).getUpdatedOn());
			Assertions.assertEquals(
					workflows.get(i).getDeletedOn(), results.get(i).getDeletedOn());
			Assertions.assertEquals(
					workflows.get(i).getNodes().size(),
					results.get(i).getNodes().size());
			for (int j = 0; j < results.get(i).getNodes().size(); j++) {
				Assertions.assertEquals(
						workflows.get(i).getNodes().get(j).getId(),
						results.get(i).getNodes().get(j).getId());
				Assertions.assertEquals(
						workflows.get(i).getNodes().get(j).getWorkflowId(),
						results.get(i).getNodes().get(j).getWorkflowId());
			}
			Assertions.assertEquals(
					workflows.get(i).getEdges().size(),
					results.get(i).getEdges().size());
			for (int j = 0; j < results.get(i).getEdges().size(); j++) {
				Assertions.assertEquals(
						workflows.get(i).getEdges().get(j).getId(),
						results.get(i).getEdges().get(j).getId());
				Assertions.assertEquals(
						workflows.get(i).getEdges().get(j).getWorkflowId(),
						results.get(i).getEdges().get(j).getWorkflowId());
			}
		}
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanSyncToNewIndex() throws Exception {

		final int NUM = 32;

		final List<Workflow> workflows = new ArrayList<>();
		for (int i = 0; i < NUM; i++) {
			workflows.add(createWorkflow(String.valueOf(i)));
		}
		workflowService.createAssets(workflows);

		final String currentIndex = workflowService.getCurrentAssetIndex();

		Assertions.assertEquals(NUM, workflowService.searchAssets(0, NUM, null).size());

		workflowService.syncAllAssetsToNewIndex(true);

		final String newIndex = workflowService.getCurrentAssetIndex();

		Assertions.assertEquals(NUM, workflowService.searchAssets(0, NUM, null).size());

		Assertions.assertNotEquals(currentIndex, newIndex);
	}
}
