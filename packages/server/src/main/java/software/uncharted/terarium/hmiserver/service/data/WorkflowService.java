package software.uncharted.terarium.hmiserver.service.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
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
			}
		}
		if (asset.getEdges() != null) {
			for (final WorkflowEdge edge : asset.getEdges()) {
				edge.setWorkflowId(asset.getId());
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
		// Fetch database copy, we will update into it
		final Workflow dbWorkflow = getAsset(asset.getId(), hasWritePermission).get();

		List<WorkflowNode<?>> dbWorkflowNodes = dbWorkflow.getNodes();
		List<WorkflowEdge> dbWorkflowEdges = dbWorkflow.getEdges();
		final Map<UUID, WorkflowNode<?>> nodeMap = new HashMap<>();
		final Map<UUID, WorkflowEdge> edgeMap = new HashMap<>();

		dbWorkflow.setName(asset.getName());
		dbWorkflow.setDescription(asset.getDescription());

		// Prep: sane state, cache the nodes/edges to update for easy retrival
		if (asset.getNodes() != null) {
			for (final WorkflowNode<?> node : asset.getNodes()) {
				node.setWorkflowId(asset.getId());
				if (node.getVersion() == null) {
					node.setVersion(1L);
				}
				nodeMap.put(node.getId(), node);

				// Debugging possible sync issue - October 2024
				log.info("Node id=" + node.getId() + ",  status=" + node.getStatus() + ", deleted=" + node.getIsDeleted());
				if (node.getOutputs() != null) {
					for (final JsonNode o : node.getOutputs()) {
						final JsonNode oValue = o.get("value").get(0);
						if (oValue != null) {
							log.info("  Out: " + oValue.asText());
						} else {
							log.info("  Out: null");
						}
					}
				} else {
					log.info("  no outputs");
				}
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

				if (nodeContent.equals(dbNodeContent)) {
					nodeMap.remove(node.getId());
					continue;
				}

				// FIXME: backwards compatibility for older workflows, remove in a few month.
				// Aug 2024
				if (dbNode.getVersion() == null) {
					dbNode.setVersion(1L);
					continue;
				}

				if (dbNode.getVersion().equals(node.getVersion())) {
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

				if (edgeContent.equals(dbEdgeContent)) {
					edgeMap.remove(edge.getId());
					continue;
				}

				// FIXME: backwards compatibility for older workflows, remove in a few month.
				// Aug 2024
				if (dbEdge.getVersion() == null) {
					dbEdge.setVersion(1L);
				}

				if (dbEdge.getVersion().equals(edge.getVersion())) {
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

		return super.updateAsset(dbWorkflow, projectId, hasWritePermission);
	}

	@Override
	protected String getAssetPath() {
		throw new UnsupportedOperationException("Workflows are not stored in S3");
	}
}
