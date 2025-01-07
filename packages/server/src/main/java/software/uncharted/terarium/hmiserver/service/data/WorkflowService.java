package software.uncharted.terarium.hmiserver.service.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.observation.annotation.Observed;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.InputPort;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.OutputPort;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.Workflow;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.WorkflowEdge;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.WorkflowNode;
import software.uncharted.terarium.hmiserver.repository.data.WorkflowRepository;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@Service
@Slf4j
public class WorkflowService extends TerariumAssetServiceWithoutSearch<Workflow, WorkflowRepository> {

	public WorkflowService(
		final ObjectMapper objectMapper,
		final Config config,
		final ProjectService projectService,
		final ProjectAssetService projectAssetService,
		final S3ClientService s3ClientService,
		final WorkflowRepository repository
	) {
		super(objectMapper, config, projectService, projectAssetService, repository, s3ClientService, Workflow.class);
	}

	@Observed(name = "function_profile")
	public Set<Workflow> findWorkflowsToClean() {
		final Set<Workflow> workflows = new HashSet<>();
		workflows.addAll(repository.findWorkflowsWithEdgesToBeDeleted());
		workflows.addAll(repository.findWorkflowsWithNodesToBeDeleted());
		return workflows;
	}

	@Observed(name = "function_profile")
	public Optional<List<Workflow>> updateWorkflows(final Collection<Workflow> assets)
		throws IOException, IllegalArgumentException {
		final List<Workflow> workflows = repository.saveAll(assets);
		return Optional.of(workflows);
	}

	@Override
	@Observed(name = "function_profile")
	public Workflow createAsset(final Workflow asset, final UUID projectId, final Schema.Permission hasWritePermission)
		throws IOException, IllegalArgumentException {
		// ensure the workflow id is set correctly
		if (asset.getNodes() != null) {
			for (final WorkflowNode<?> node : asset.getNodes()) {
				node.setWorkflowId(asset.getId());
				if (node.getVersion() == null) {
					node.setVersion(1L);
				}
			}
		}
		if (asset.getEdges() != null) {
			for (final WorkflowEdge edge : asset.getEdges()) {
				edge.setWorkflowId(asset.getId());
				if (edge.getVersion() == null) {
					edge.setVersion(1L);
				}
			}
		}
		return super.createAsset(asset, projectId, hasWritePermission);
	}

	@Override
	@Observed(name = "function_profile")
	public Optional<Workflow> updateAsset(
		final Workflow asset,
		final UUID projectId,
		final Schema.Permission hasWritePermission
	) throws IOException, IllegalArgumentException {
		final long updateStart = System.currentTimeMillis();
		// Fetch database copy, we will update into it
		final Workflow dbWorkflow = getAsset(asset.getId(), hasWritePermission).get();

		List<WorkflowNode<?>> dbWorkflowNodes = dbWorkflow.getNodes();
		List<WorkflowEdge> dbWorkflowEdges = dbWorkflow.getEdges();
		final Map<UUID, WorkflowNode<?>> nodeMap = new HashMap<>();
		final Map<UUID, WorkflowEdge> edgeMap = new HashMap<>();

		dbWorkflow.setName(asset.getName());
		dbWorkflow.setDescription(asset.getDescription());
		dbWorkflow.setScenario(asset.getScenario());

		// Prep: sane state, cache the nodes/edges to update for easy retrival
		if (asset.getNodes() != null) {
			for (final WorkflowNode<?> node : asset.getNodes()) {
				node.setWorkflowId(asset.getId());
				if (node.getVersion() == null) {
					node.setVersion(1L);
				}
				nodeMap.put(node.getId(), node);
			}
		}
		if (asset.getEdges() != null) {
			for (final WorkflowEdge edge : asset.getEdges()) {
				edge.setWorkflowId(asset.getId());
				if (edge.getVersion() == null) {
					edge.setVersion(1L);
				}
				edgeMap.put(edge.getId(), edge);
			}
		}

		////////////////////////////////////////////////////////////////////////////////
		// Handle updates and deletes (tombdstoning)
		////////////////////////////////////////////////////////////////////////////////
		if (dbWorkflowNodes != null && dbWorkflowNodes.size() > 0) {
			for (int index = 0; index < dbWorkflowNodes.size(); index++) {
				final WorkflowNode<?> dbNode = dbWorkflowNodes.get(index);
				final WorkflowNode<?> node = nodeMap.get(dbNode.getId());

				if (node == null) continue;

				final JsonNode nodeContent = this.objectMapper.valueToTree(node);
				final JsonNode dbNodeContent = this.objectMapper.valueToTree(dbNode);

				// No changes, skip
				if (nodeContent.equals(dbNodeContent)) {
					nodeMap.remove(node.getId());
					continue;
				}

				// Only update if if node is not already deleted in the db
				if (dbNode.getIsDeleted() == false && dbNode.getVersion().equals(node.getVersion())) {
					node.setVersion(dbNode.getVersion() + 1L);
					dbWorkflowNodes.set(index, node);
				}

				// remove once updated
				nodeMap.remove(node.getId());
			}
		}

		if (dbWorkflowEdges != null && dbWorkflowEdges.size() > 0) {
			for (int index = 0; index < dbWorkflowEdges.size(); index++) {
				final WorkflowEdge dbEdge = dbWorkflowEdges.get(index);
				final WorkflowEdge edge = edgeMap.get(dbEdge.getId());

				if (edge == null) continue;

				final JsonNode edgeContent = this.objectMapper.valueToTree(edge);
				final JsonNode dbEdgeContent = this.objectMapper.valueToTree(dbEdge);

				// No changes, skip
				if (edgeContent.equals(dbEdgeContent)) {
					edgeMap.remove(edge.getId());
					continue;
				}

				// Only update if if edge is not already deleted in the db
				if (dbEdge.getIsDeleted() == false && dbEdge.getVersion().equals(edge.getVersion())) {
					edge.setVersion(dbEdge.getVersion() + 1L);
					dbWorkflowEdges.set(index, edge);
				}

				// remove once updated
				edgeMap.remove(edge.getId());
			}
		}

		////////////////////////////////////////////////////////////////////////////////
		// Handle new nodes or edges
		////////////////////////////////////////////////////////////////////////////////
		if (dbWorkflowNodes == null) {
			dbWorkflowNodes = new ArrayList<>();
		}
		for (final Map.Entry<UUID, WorkflowNode<?>> pair : nodeMap.entrySet()) {
			dbWorkflowNodes.add(pair.getValue());
		}

		if (dbWorkflowEdges == null) {
			dbWorkflowEdges = new ArrayList<>();
		}
		for (final Map.Entry<UUID, WorkflowEdge> pair : edgeMap.entrySet()) {
			dbWorkflowEdges.add(pair.getValue());
		}

		final long resolveEnd = System.currentTimeMillis();
		log.info("Resolve workflow " + dbWorkflow.getId() + " took " + (resolveEnd - updateStart));

		final Optional<Workflow> result = super.updateAsset(dbWorkflow, projectId, hasWritePermission);

		final long updateEnd = System.currentTimeMillis();
		log.info("Update workflow to DB " + dbWorkflow.getId() + " took " + (updateEnd - resolveEnd));

		return result;
	}

	@Observed(name = "function_profile")
	private void cascadeInvalidStatus(final WorkflowNode<?> sourceNode, final Map<UUID, List<WorkflowNode>> nodeCache) {
		List<WorkflowNode> downstreamNodes = nodeCache.get(sourceNode.getId());
		for (final WorkflowNode node : downstreamNodes) {
			node.setStatus("invalid");
			cascadeInvalidStatus(node, nodeCache);
		}
	}

	/**
	 * Updates the operator's state using the data from a specified WorkflowOutput. If the operator's
	 * current state was not previously stored as a WorkflowOutput, this function first saves the current state
	 * as a new WorkflowOutput. It then replaces the operator's existing state with the data from the specified WorkflowOutput.
	 *
	 **/
	@Observed(name = "function_profile")
	public void selectOutput(final Workflow workflow, final WorkflowNode<?> operator, UUID selectedId) throws Exception {
		for (final OutputPort port : operator.getOutputs()) {
			port.setIsSelected(false);
			port.setStatus("not connected");
		}

		// Update the Operator state with the selected one
		final OutputPort selected = operator
			.getOutputs()
			.stream()
			.filter(port -> port.getId() == selectedId)
			.findAny()
			.orElse(null);

		if (selected == null) {
			throw new Exception("Cannot find port " + selectedId);
		}

		selected.setIsSelected(true);
		operator.setState(deepMergeWithOverwrite(operator.getState(), selected.getState()));
		operator.setStatus(selected.getOperatorStatus());
		operator.setActive(selected.getId());

		// If this output is connected to input port(s), update the input port(s)
		for (final WorkflowEdge edge : workflow.getEdges()) {
			if (edge.getIsDeleted() != false && edge.getSource() == operator.getId()) {
				return;
			}
		}

		selected.setStatus("connected");

		final Map<UUID, WorkflowNode<?>> nodeMap = new HashMap<>();
		final Map<UUID, List<WorkflowNode<?>>> nodeCache = new HashMap<>();
		for (final WorkflowNode<?> node : workflow.getNodes()) {
			if (node.getIsDeleted() == false) {
				nodeMap.put(node.getId(), node);
			}
		}
		nodeCache.put(selected.getId(), new ArrayList());

		for (final WorkflowEdge edge : workflow.getEdges()) {
			if (edge.getIsDeleted() == true) continue;

			if (edge.getSource() == operator.getId()) {
				final WorkflowNode<?> targetNode = workflow
					.getNodes()
					.stream()
					.filter(node -> node.getId() == edge.getTarget())
					.findAny()
					.orElse(null);
				if (targetNode == null) continue;

				// Update the input port of the target node
				final InputPort targetPort = targetNode
					.getInputs()
					.stream()
					.filter(port -> port.getId() == edge.getTargetPortId())
					.findAny()
					.orElse(null);
				if (targetPort == null) continue;

				// Sync edge source port to selected output
				edge.setSourcePortId(selected.getId());

				//
				// Handle compound type unwrangling, this is very similar to what
				// we do in addEdge(...) but s already connected.
				///
				final List<String> selectedTypes = splitAndTrim(selected.getType());
				final List<String> allowedInputTypes = splitAndTrim(targetPort.getType());
				final List<String> intersectionTypes = findIntersection(selectedTypes, allowedInputTypes);

				// Sanity check: multiple matches found
				if (intersectionTypes.size() > 1) {
					log.warn("Ambiguous matching types " + selectedTypes + " " + allowedInputTypes);
					continue;
				}

				// Sanity check: no matches found
				if (intersectionTypes.size() == 0) {
					log.warn("No matching types " + selectedTypes + " " + allowedInputTypes);
					continue;
				}

				// Finally we should be okay at this point
				if (selectedTypes.size() > 1) {
					String concreteType = intersectionTypes.get(0);
					if (selected.getValue() != null) {
						ArrayNode arrayNode = objectMapper.createArrayNode();
						arrayNode.add(selected.getValue().get(0).get(concreteType));
						targetPort.setValue(arrayNode);
					}
				} else {
					targetPort.setValue(selected.getValue());
				}
				targetPort.setLabel(selected.getLabel());
			}

			// Collect node cache
			if (edge.getSource() == null || edge.getTarget() == null) continue;
			if (!nodeCache.containsKey(edge.getSource())) {
				nodeCache.put(edge.getSource(), new ArrayList());
			}
			nodeCache.get(edge.getSource()).add(nodeMap.get(edge.getTarget()));
		}
	}

	@Override
	protected String getAssetPath() {
		throw new UnsupportedOperationException("Workflows are not stored in S3");
	}

	public static JsonNode deepMergeWithOverwrite(JsonNode nodeA, JsonNode nodeB) {
		if (nodeA.isObject() && nodeB.isObject()) {
			ObjectNode objectA = (ObjectNode) nodeA;
			nodeB
				.fields()
				.forEachRemaining(entry -> {
					String fieldName = entry.getKey();
					JsonNode valueB = entry.getValue();
					if (objectA.has(fieldName)) {
						JsonNode valueA = objectA.get(fieldName);
						objectA.set(fieldName, deepMergeWithOverwrite(valueA, valueB));
					} else {
						objectA.set(fieldName, valueB);
					}
				});
			return objectA;
		}
		// If there's a conflict or non-object nodes, `B` overwrites `A`
		return nodeB;
	}

	public static List<String> splitAndTrim(String input) {
		return Arrays.stream(input.split("\\|")).map(String::trim).collect(Collectors.toList());
	}

	public static List<String> findIntersection(List<String> list1, List<String> list2) {
		List<String> intersection = new ArrayList<>(list1);
		intersection.retainAll(list2); // Retain only elements that are in both lists
		return intersection;
	}
}
