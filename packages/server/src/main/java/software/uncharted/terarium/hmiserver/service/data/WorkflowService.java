package software.uncharted.terarium.hmiserver.service.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.Workflow;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.WorkflowEdge;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.WorkflowNode;
import software.uncharted.terarium.hmiserver.repository.data.WorkflowRepository;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@Service
public class WorkflowService extends TerariumAssetServiceWithSearch<Workflow, WorkflowRepository> {

	public WorkflowService(
		final ObjectMapper objectMapper,
		final Config config,
		final ElasticsearchConfiguration elasticConfig,
		final ElasticsearchService elasticService,
		final ProjectService projectService,
		final ProjectAssetService projectAssetService,
		final S3ClientService s3ClientService,
		final WorkflowRepository repository
	) {
		super(
			objectMapper,
			config,
			elasticConfig,
			elasticService,
			projectService,
			projectAssetService,
			s3ClientService,
			repository,
			Workflow.class
		);
	}

	@Override
	@Observed(name = "function_profile")
	protected String getAssetIndex() {
		return elasticConfig.getWorkflowIndex();
	}

	@Override
	@Observed(name = "function_profile")
	public String getAssetAlias() {
		return elasticConfig.getWorkflowAlias();
	}

	@Override
	@Observed(name = "function_profile")
	public Workflow createAsset(final Workflow asset, final UUID projectId, final Schema.Permission hasWritePermission)
		throws IOException, IllegalArgumentException {
		// ensure the workflow id is set correctly
		if (asset.getNodes() != null) {
			for (final WorkflowNode node : asset.getNodes()) {
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

	@Observed(name = "function_profile")
	public Optional<Workflow> updateAsset(
		final Workflow asset,
		final UUID nodeId,
		final UUID projectId,
		final Schema.Permission hasWritePermission
	) throws IOException, IllegalArgumentException {
		final Workflow dbWorkflow = getAsset(asset.getId(), hasWritePermission).get();
	}

	@Override
	@Observed(name = "function_profile")
	public Optional<Workflow> updateAsset(
		final Workflow asset,
		final UUID projectId,
		final Schema.Permission hasWritePermission
	) throws IOException, IllegalArgumentException {
		// ensure the workflow id is set correctly
		// if (asset.getEdges() != null) {
		// 	for (final WorkflowEdge edge : asset.getEdges()) {
		// 		edge.setWorkflowId(asset.getId());
		// 	}
		// }

		final Workflow dbWorkflow = getAsset(asset.getId(), hasWritePermission).get();

		// Iterate through nodes and edges and check if updates can be done
		// - If the nodes are different, then the version we want to write must have
		//   version >= db node's version
		final List<WorkflowNode> dbWorkflowNodes = dbWorkflow.getNodes();
		final Map<UUID, WorkflowNode> nodeMap = new HashMap();
		if (asset.getNodes() != null) {
			for (final WorkflowNode node : asset.getNodes()) {
				node.setWorkflowId(asset.getId());
				nodeMap.put(node.getId(), node);
			}
		}

		for (int index = 0; index < dbWorkflowNodes.size(); index++) {
			WorkflowNode dbNode = dbWorkflowNodes.get(index);
			WorkflowNode node = nodeMap.get(dbNode.getId());

			if (node == null) continue;
			JsonNode nodeContent = this.objectMapper.valueToTree(node);
			JsonNode dbNodeContent = this.objectMapper.valueToTree(dbNode);

			if (nodeContent.equals(dbNodeContent) == true) continue;

			// FIXME: backwards compatibility for older workflows, remove in a few month. Aug 2024
			if (dbNode.getVersion() == null) {
				dbNode.setVersion(1L);
			}
			if (node.getVersion() == null) {
				node.setVersion(1L);
			}

			if (dbNode.getVersion() == node.getVersion()) {
				node.setVersion(dbNode.getVersion() + 1L);
				dbWorkflowNodes.set(index, node);
			}
			// mark as done
			nodeMap.remove(node.getId());
		}
		// The remaining ones are additions
		for (Map.Entry<UUID, WorkflowNode> pair : nodeMap.entrySet()) {
			dbWorkflowNodes.add(pair.getValue());
		}

		final List<WorkflowEdge> dbWorkflowEdges = dbWorkflow.getEdges();
		final Map<UUID, WorkflowEdge> edgeMap = new HashMap();
		if (asset.getEdges() != null) {
			for (final WorkflowEdge edge : asset.getEdges()) {
				edge.setWorkflowId(asset.getId());
				edgeMap.put(edge.getId(), edge);
			}
		}

		for (int index = 0; index < dbWorkflowEdges.size(); index++) {
			WorkflowEdge dbEdge = dbWorkflowEdges.get(index);
			WorkflowEdge edge = edgeMap.get(dbEdge.getId());

			if (edge == null) continue;
			JsonNode edgeContent = this.objectMapper.valueToTree(edge);
			JsonNode dbEdgeContent = this.objectMapper.valueToTree(dbEdge);

			if (edgeContent.equals(dbEdgeContent) == true) continue;

			// FIXME: backwards compatibility for older workflows, remove in a few month. Aug 2024
			if (dbEdge.getVersion() == null) {
				dbEdge.setVersion(1L);
			}
			if (edge.getVersion() == null) {
				edge.setVersion(1L);
			}

			if (dbEdge.getVersion() == edge.getVersion()) {
				edge.setVersion(dbEdge.getVersion() + 1L);
				dbWorkflowEdges.set(index, edge);
			}
			// mark as done
			edgeMap.remove(edge.getId());
		}
		// The remaining ones are additions
		for (Map.Entry<UUID, WorkflowEdge> pair : edgeMap.entrySet()) {
			dbWorkflowEdges.add(pair.getValue());
		}

		/*
		final List<WorkflowNode> candidateWorkflowNodes = asset.getNodes();
		for (WorkflowNode node : candidateWorkflowNodes) {
			WorkflowNode dbNode = dbWorkflowNodeMap.get(node.getId());
			if (dbNode == null) continue;

			if (node.equals(dbNode) == true) {
				System.out.println(" lombok: " + node.getDisplayName() + " equal");
			} else {
				System.out.println(" lombok: " + node.getDisplayName() + " not equal");
			}
		}*/
		return super.updateAsset(dbWorkflow, projectId, hasWritePermission);
	}

	@Override
	protected String getAssetPath() {
		throw new UnsupportedOperationException("Workflows are not stored in S3");
	}
}
