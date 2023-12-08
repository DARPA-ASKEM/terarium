package software.uncharted.terarium.hmiserver.service.data;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.Workflow;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkflowService {

	private final ElasticsearchService elasticService;
	private final ElasticsearchConfiguration elasticConfig;

	public List<Workflow> getWorkflows(final Integer page, final Integer pageSize) throws IOException {
		final SearchRequest req = new SearchRequest.Builder()
			.index(elasticConfig.getWorkflowIndex())
			.from(page)
			.size(pageSize)
			.build();
		return elasticService.search(req, Workflow.class);
	}

	public Workflow getWorkflow(final String id) throws IOException {
		return elasticService.get(elasticConfig.getWorkflowIndex(), id, Workflow.class);
	}

	public void deleteWorkflow(final String id) throws IOException {
		elasticService.delete(elasticConfig.getWorkflowIndex(), id);
	}

	public Workflow createWorkflow(final Workflow workflow) throws IOException {
		elasticService.index(elasticConfig.getWorkflowIndex(), workflow.getId(), workflow);
		return workflow;
	}

	public Workflow updateWorkflow(final String id, final Workflow workflow) throws IOException, IllegalArgumentException {
		if (!id.equals(workflow.getId())) {
			throw new IllegalArgumentException("Workflow ID does not match Workflow object ID");
		}
		elasticService.index(elasticConfig.getWorkflowIndex(), workflow.getId(), workflow);
		return workflow;
	}
}
