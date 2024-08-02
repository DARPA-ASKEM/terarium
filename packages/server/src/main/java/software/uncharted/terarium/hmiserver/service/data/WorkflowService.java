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

	@Override
	@Observed(name = "function_profile")
	public Optional<Workflow> updateAsset(
		final Workflow asset,
		final UUID projectId,
		final Schema.Permission hasWritePermission
	) throws IOException, IllegalArgumentException {
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

		long startTime = System.currentTimeMillis();

		System.out.println("");
		System.out.println("");

		final Workflow dbWorkflow = getAsset(asset.getId(), hasWritePermission).get();

		// Iterate through nodes and edges and check if updates can be done
		// - If the nodes are different, then the version we want to write must have
		//   version >= db node's version
		final List<WorkflowNode> dbWorkflowNodes = dbWorkflow.getNodes();
		final Map<UUID, WorkflowNode> dbWorkflowNodeMap = new HashMap();
		for (WorkflowNode node : dbWorkflowNodes) {
			dbWorkflowNodeMap.put(node.getId(), node);
		}

		final List<WorkflowNode> candidateWorkflowNodes = asset.getNodes();
		for (WorkflowNode node : candidateWorkflowNodes) {
			WorkflowNode dbNode = dbWorkflowNodeMap.get(node.getId());
			if (dbNode == null) continue;

			if (node.equals(dbNode) == true) {
				System.out.println(" lombok: " + node.getDisplayName() + " equal");
			} else {
				System.out.println(" lombok: " + node.getDisplayName() + " not equal");
			}
		}

		System.out.println("");

		for (WorkflowNode node : candidateWorkflowNodes) {
			WorkflowNode dbNode = dbWorkflowNodeMap.get(node.getId());
			if (dbNode == null) continue;

			JsonNode nodeContent = this.objectMapper.valueToTree(node);
			JsonNode dbNodeContent = this.objectMapper.valueToTree(dbNode);

			if (nodeContent.equals(dbNodeContent) == true) {
				System.out.println(" mapper " + node.getDisplayName() + " equal");
			} else {
				System.out.println(" mapper " + node.getDisplayName() + " not equal");
				System.out.println(" node: ");
				System.out.println(nodeContent);
				System.out.println(" dbNode: ");
				System.out.println(dbNodeContent);
			}
		}

		// if nodeA != nodeB && nodeA.version >= nodeB.version
		//   // then we can update
		// else
		//   // rturn 409??  420

		long endTime = System.currentTimeMillis();
		System.out.println("Elapsed: " + (endTime - startTime));
		System.out.println("");
		System.out.println("");

		return super.updateAsset(asset, projectId, hasWritePermission);
	}

	@Override
	protected String getAssetPath() {
		throw new UnsupportedOperationException("Workflows are not stored in S3");
	}
}
