package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.util.Optional;

import org.springframework.stereotype.Service;

import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.Workflow;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.WorkflowEdge;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.WorkflowNode;
import software.uncharted.terarium.hmiserver.repository.data.WorkflowRepository;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

@Service
public class WorkflowService extends TerariumAssetServiceWithSearch<Workflow, WorkflowRepository> {

	public WorkflowService(
			final Config config,
			final ElasticsearchConfiguration elasticConfig,
			final ElasticsearchService elasticService,
			final ProjectAssetService projectAssetService,
			final WorkflowRepository repository) {
		super(config, elasticConfig, elasticService, projectAssetService, repository, Workflow.class);
	}

	@Override
	protected String getAssetIndex() {
		return elasticConfig.getWorkflowIndex();
	}

	@Override
	public String getAssetAlias() {
		return elasticConfig.getWorkflowAlias();
	}

	@Override
	public Workflow createAsset(final Workflow asset) throws IOException, IllegalArgumentException {
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
		return super.createAsset(asset);
	}

	@Override
	public Optional<Workflow> updateAsset(final Workflow asset) throws IOException, IllegalArgumentException {
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
		return super.updateAsset(asset);
	}
}
