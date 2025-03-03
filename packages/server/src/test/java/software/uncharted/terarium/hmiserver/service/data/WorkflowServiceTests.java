package software.uncharted.terarium.hmiserver.service.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.test.context.support.WithUserDetails;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.InputPort;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.OutputPort;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.Transform;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.Workflow;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.WorkflowEdge;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.WorkflowNode;

@Slf4j
public class WorkflowServiceTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper mapper;

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

	static Workflow createWorkflow() throws Exception {
		final WorkflowNode a = new WorkflowNode().setId(UUID.randomUUID());
		final WorkflowNode b = new WorkflowNode().setId(UUID.randomUUID());
		final WorkflowNode c = new WorkflowNode().setId(UUID.randomUUID());
		final WorkflowNode d = new WorkflowNode().setId(UUID.randomUUID());

		final WorkflowEdge ab = new WorkflowEdge().setSource(a.getId()).setTarget(b.getId());
		final WorkflowEdge bc = new WorkflowEdge().setSource(b.getId()).setTarget(c.getId());
		final WorkflowEdge cd = new WorkflowEdge().setSource(c.getId()).setTarget(d.getId());

		return (Workflow) new Workflow()
			.setTransform(new Transform().setX(1).setY(2).setK(3))
			.setNodes(List.of(a, b, c, d))
			.setEdges(List.of(ab, bc, cd))
			.setPublicAsset(true)
			.setDescription("test-workflow-description-0")
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

	// Full fledged workflow from a file
	static Workflow createWorkflowFromFile() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		final ClassPathResource resource = new ClassPathResource("workflow/workflow1.json");
		final String content = new String(Files.readAllBytes(resource.getFile().toPath()));
		final Workflow wf = mapper.readValue(content, Workflow.class);
		return wf;
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanParseWorkflow() throws Exception {
		final Workflow wf = createWorkflowFromFile();
		Assertions.assertNotNull(wf.getName());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanBranchWorkflow() throws Exception {
		final Workflow wf1 = createWorkflowFromFile();
		final WorkflowNode modelConfigNode = wf1
			.getNodes()
			.stream()
			.filter(n -> n.getOperationType().equals("ModelConfiguration"))
			.findFirst()
			.orElse(null);

		Assertions.assertNotNull(modelConfigNode);
		workflowService.branchWorkflow(wf1, modelConfigNode.getId(), null);
		Assertions.assertEquals(wf1.getNodes().size(), 5);
		Assertions.assertEquals(wf1.getEdges().size(), 4);

		final Workflow wf2 = createWorkflowFromFile();
		final WorkflowNode modelNode = wf2
			.getNodes()
			.stream()
			.filter(n -> n.getOperationType().equals("ModelOperation"))
			.findFirst()
			.orElse(null);

		Assertions.assertNotNull(modelNode);
		workflowService.branchWorkflow(wf2, modelNode.getId(), null);
		Assertions.assertEquals(wf2.getNodes().size(), 6);
		Assertions.assertEquals(wf2.getEdges().size(), 4);
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteEdges() throws Exception {
		final Workflow wf1 = createWorkflowFromFile();

		final UUID edgeId = UUID.fromString("9f2f5783-8e9a-4081-91cb-063774392321");
		List<UUID> edgeIds = new ArrayList<UUID>();

		edgeIds.add(edgeId);
		workflowService.removeEdges(wf1, edgeIds);
		final long edgeCount = wf1.getEdges().stream().filter(e -> e.getIsDeleted() == false).count();
		final WorkflowNode simulateNode = wf1
			.getNodes()
			.stream()
			.filter(n -> n.getId().equals(UUID.fromString("1c63929a-5aad-4e15-bbf0-e1e6ab0d5d14")))
			.findFirst()
			.orElse(null);
		final WorkflowNode configNode = wf1
			.getNodes()
			.stream()
			.filter(n -> n.getId().equals(UUID.fromString("e459cf71-b788-4e3b-8161-ac586079fd20")))
			.findFirst()
			.orElse(null);

		Assertions.assertEquals(edgeCount, 1);
		Assertions.assertNotNull(simulateNode);
		Assertions.assertNotNull(configNode);
		Assertions.assertEquals(simulateNode.getStatus(), "invalid");
		Assertions.assertEquals(configNode.getStatus(), "invalid");
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanHandleInvalidDeleteEdges() throws Exception {
		final Workflow wf1 = createWorkflowFromFile();

		final UUID edgeId = UUID.randomUUID();
		List<UUID> edgeIds = new ArrayList<UUID>();

		edgeIds.add(edgeId);
		workflowService.removeEdges(wf1, edgeIds);
		final long edgeCount = wf1.getEdges().stream().filter(e -> e.getIsDeleted() == false).count();
		final WorkflowNode simulateNode = wf1
			.getNodes()
			.stream()
			.filter(n -> n.getId().equals(UUID.fromString("1c63929a-5aad-4e15-bbf0-e1e6ab0d5d14")))
			.findFirst()
			.orElse(null);
		final WorkflowNode configNode = wf1
			.getNodes()
			.stream()
			.filter(n -> n.getId().equals(UUID.fromString("e459cf71-b788-4e3b-8161-ac586079fd20")))
			.findFirst()
			.orElse(null);

		Assertions.assertEquals(edgeCount, 2);
		Assertions.assertNotNull(simulateNode);
		Assertions.assertNotNull(configNode);
		Assertions.assertEquals(simulateNode.getStatus(), "success");
		Assertions.assertEquals(configNode.getStatus(), "success");
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanHandleValidDisjunctiveTypes() throws Exception {
		final Workflow wf = new Workflow();
		final WorkflowNode start = new WorkflowNode();
		final WorkflowNode end = new WorkflowNode();
		final OutputPort out = new OutputPort();
		final InputPort in = new InputPort();
		final WorkflowEdge edge = new WorkflowEdge();

		wf.setNodes(new ArrayList<WorkflowNode>());
		wf.setEdges(new ArrayList<WorkflowEdge>());

		start.setInputs(new ArrayList<InputPort>());
		start.setOutputs(new ArrayList<OutputPort>());
		start.setUniqueInputs(false);

		end.setInputs(new ArrayList<InputPort>());
		end.setOutputs(new ArrayList<OutputPort>());
		end.setUniqueInputs(false);

		start.setId(UUID.randomUUID());
		start.getOutputs().add(out);

		end.setId(UUID.randomUUID());
		end.getInputs().add(in);

		out.setId(UUID.randomUUID());
		out.setStatus("not connected");

		in.setId(UUID.randomUUID());
		in.setStatus("not connected");

		edge.setId(UUID.randomUUID());
		edge.setSource(start.getId());
		edge.setSourcePortId(out.getId());
		edge.setTarget(end.getId());
		edge.setTargetPortId(in.getId());

		workflowService.addNode(wf, start);
		workflowService.addNode(wf, end);

		final ArrayNode arr = mapper.createArrayNode();
		final ObjectNode val = mapper.createObjectNode();
		val.put("id", "abcdef");
		arr.add(val);

		out.setType("model");
		out.setValue(arr);
		in.setType("dataset|model");

		workflowService.addEdge(wf, edge);

		final long edgeCount = wf.getEdges().stream().filter(e -> e.getIsDeleted() == false).count();
		Assertions.assertEquals(edgeCount, 1);

		final ObjectNode inVal = (ObjectNode) in.getValue().get(0);
		Assertions.assertEquals(inVal.get("id").asText(), "abcdef");
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanHandleInvalidDisjunctiveTypes() throws Exception {
		final Workflow wf = new Workflow();
		final WorkflowNode start = new WorkflowNode();
		final WorkflowNode end = new WorkflowNode();
		final OutputPort out = new OutputPort();
		final InputPort in = new InputPort();
		final WorkflowEdge edge = new WorkflowEdge();

		wf.setNodes(new ArrayList<WorkflowNode>());
		wf.setEdges(new ArrayList<WorkflowEdge>());

		start.setInputs(new ArrayList<InputPort>());
		start.setOutputs(new ArrayList<OutputPort>());
		start.setUniqueInputs(false);

		end.setInputs(new ArrayList<InputPort>());
		end.setOutputs(new ArrayList<OutputPort>());
		end.setUniqueInputs(false);

		start.setId(UUID.randomUUID());
		start.getOutputs().add(out);

		end.setId(UUID.randomUUID());
		end.getInputs().add(in);

		out.setId(UUID.randomUUID());
		out.setStatus("not connected");

		in.setId(UUID.randomUUID());
		in.setStatus("not connected");

		edge.setId(UUID.randomUUID());
		edge.setSource(start.getId());
		edge.setSourcePortId(out.getId());
		edge.setTarget(end.getId());
		edge.setTargetPortId(in.getId());

		workflowService.addNode(wf, start);
		workflowService.addNode(wf, end);

		final ArrayNode arr = mapper.createArrayNode();
		final ObjectNode val = mapper.createObjectNode();
		val.put("id", "abcdef");
		arr.add(val);

		out.setType("not_model");
		out.setValue(arr);
		in.setType("dataset|model");

		workflowService.addEdge(wf, edge);

		final long edgeCount = wf.getEdges().stream().filter(e -> e.getIsDeleted() == false).count();
		Assertions.assertEquals(edgeCount, 0);
		Assertions.assertEquals(in.getValue(), null);
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateWorkflow() throws Exception {
		final Workflow before = (Workflow) createWorkflow().setId(UUID.randomUUID());
		final Workflow after = workflowService.createAsset(before, project.getId());

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

		workflowService.createAsset(workflow, project.getId());

		try {
			workflowService.createAsset(workflow, project.getId());
			Assertions.fail("Should have thrown an exception");
		} catch (final IllegalArgumentException e) {
			Assertions.assertTrue(e.getMessage().contains("already exists"));
		}
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetWorkflows() throws Exception {
		workflowService.createAsset(createWorkflow("0"), project.getId());
		workflowService.createAsset(createWorkflow("1"), project.getId());
		workflowService.createAsset(createWorkflow("2"), project.getId());

		final List<Workflow> workflows = workflowService.getPublicNotTemporaryAssets(0, 3);

		Assertions.assertEquals(3, workflows.size());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetWorkflow() throws Exception {
		final Workflow workflow = workflowService.createAsset(createWorkflow(), project.getId());

		final Workflow fetchedWorkflow = workflowService.getAsset(workflow.getId()).get();

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
		final Workflow workflow = workflowService.createAsset(createWorkflow(), project.getId());
		workflow.setName("new name");

		final Workflow updatedWorkflow = workflowService.updateAsset(workflow, project.getId()).orElseThrow();

		Assertions.assertEquals(workflow, updatedWorkflow);
		Assertions.assertNotNull(updatedWorkflow.getUpdatedOn());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteWorkflow() throws Exception {
		final Workflow workflow = workflowService.createAsset(createWorkflow(), project.getId());

		workflowService.deleteAsset(workflow.getId(), project.getId());

		final Optional<Workflow> deleted = workflowService.getAsset(workflow.getId());

		Assertions.assertTrue(deleted.isEmpty());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCloneWorkflow() throws Exception {
		Workflow workflow = createWorkflow();
		workflow = workflowService.createAsset(workflow, project.getId());

		final Workflow cloned = workflow.clone();

		Assertions.assertNotEquals(workflow.getId(), cloned.getId());
		Assertions.assertEquals(workflow.getNodes().size(), cloned.getNodes().size());
		Assertions.assertNotEquals(workflow.getNodes().get(0).getId(), cloned.getNodes().get(0).getId());
		Assertions.assertNotEquals(workflow.getNodes().get(0).getWorkflowId(), cloned.getNodes().get(0).getWorkflowId());
		Assertions.assertNotEquals(workflow.getNodes().get(1).getId(), cloned.getNodes().get(1).getId());
		Assertions.assertNotEquals(workflow.getNodes().get(1).getWorkflowId(), cloned.getNodes().get(1).getWorkflowId());
		Assertions.assertNotEquals(workflow.getNodes().get(2).getId(), cloned.getNodes().get(2).getId());
		Assertions.assertNotEquals(workflow.getNodes().get(2).getWorkflowId(), cloned.getNodes().get(2).getWorkflowId());
		Assertions.assertNotEquals(workflow.getNodes().get(3).getId(), cloned.getNodes().get(3).getId());
		Assertions.assertNotEquals(workflow.getNodes().get(3).getWorkflowId(), cloned.getNodes().get(3).getWorkflowId());
		Assertions.assertEquals(workflow.getEdges().size(), cloned.getEdges().size());
		Assertions.assertNotEquals(workflow.getEdges().get(0).getId(), cloned.getEdges().get(0).getId());
		Assertions.assertNotEquals(workflow.getEdges().get(0).getWorkflowId(), cloned.getEdges().get(0).getWorkflowId());
		Assertions.assertNotEquals(workflow.getEdges().get(1).getId(), cloned.getEdges().get(1).getId());
		Assertions.assertNotEquals(workflow.getEdges().get(1).getWorkflowId(), cloned.getEdges().get(1).getWorkflowId());
		Assertions.assertNotEquals(workflow.getEdges().get(2).getId(), cloned.getEdges().get(2).getId());
		Assertions.assertNotEquals(workflow.getEdges().get(2).getWorkflowId(), cloned.getEdges().get(2).getWorkflowId());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testWorkflowsAreOpaque() throws Exception {
		final WorkflowNode a = mapper.readValue(
			"{\"id\":\"" + UUID.randomUUID() + "\", \"otherField\": 123 }",
			WorkflowNode.class
		);

		final WorkflowNode b = mapper.readValue(
			"{\"id\":\"" + UUID.randomUUID() + "\", \"anotherField\": \"text value\" }",
			WorkflowNode.class
		);

		final WorkflowEdge e = mapper.readValue(
			"{\"id\":\"" +
			UUID.randomUUID() +
			"\"" +
			", \"source\": \"" +
			a.getId() +
			"\"" +
			", \"target\": \"" +
			b.getId() +
			"\"" +
			", \"somethingElse\": \"some value\"}",
			WorkflowEdge.class
		);

		Workflow workflow = new Workflow().setNodes(List.of(a, b)).setEdges(List.of(e));
		workflow.setPublicAsset(true);

		workflow = workflowService.createAsset(workflow, project.getId());

		JsonNode raw = mapper.valueToTree(workflow);
		raw
			.get("nodes")
			.forEach(n -> {
				Assertions.assertTrue(n.has("otherField") || n.has("anotherField"));
			});
		raw
			.get("edges")
			.forEach(n -> {
				Assertions.assertTrue(n.has("somethingElse"));
			});

		final Workflow cloned = workflow.clone();

		Assertions.assertNotEquals(workflow.getId(), cloned.getId());
		Assertions.assertEquals(workflow.getNodes().size(), cloned.getNodes().size());
		Assertions.assertNotEquals(workflow.getNodes().get(0).getId(), cloned.getNodes().get(0).getId());
		Assertions.assertNotEquals(workflow.getNodes().get(1).getId(), cloned.getNodes().get(1).getId());
		Assertions.assertEquals(workflow.getEdges().size(), cloned.getEdges().size());
		Assertions.assertNotEquals(workflow.getEdges().get(0).getId(), cloned.getEdges().get(0).getId());

		// ensure additional fields are preserved on clone
		raw = mapper.valueToTree(workflow);
		raw
			.get("nodes")
			.forEach(n -> {
				Assertions.assertTrue(n.has("otherField") || n.has("anotherField"));
			});
		raw
			.get("edges")
			.forEach(n -> {
				Assertions.assertTrue(n.has("somethingElse"));
			});
	}
}
