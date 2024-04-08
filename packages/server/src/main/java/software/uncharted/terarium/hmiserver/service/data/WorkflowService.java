package software.uncharted.terarium.hmiserver.service.data;

import org.springframework.stereotype.Service;

import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.Workflow;
import software.uncharted.terarium.hmiserver.repository.data.WorkflowRepository;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

@Service
public class WorkflowService extends TerariumAssetServiceWithES<Workflow, WorkflowRepository> {

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
}
