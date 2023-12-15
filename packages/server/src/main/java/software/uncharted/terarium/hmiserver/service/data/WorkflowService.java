package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import lombok.RequiredArgsConstructor;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.Workflow;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

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

	public Workflow getWorkflow(final UUID id) throws IOException {
		return elasticService.get(elasticConfig.getWorkflowIndex(), id.toString(), Workflow.class);
	}

	public void deleteWorkflow(final UUID id) throws IOException {
		elasticService.delete(elasticConfig.getWorkflowIndex(), id.toString());
	}

	public Workflow createWorkflow(final Workflow workflow) throws IOException {
		elasticService.index(elasticConfig.getWorkflowIndex(), workflow.setId(UUID.randomUUID()).getId().toString(),
				workflow);
		return workflow;
	}

	public Workflow updateWorkflow(final UUID id, final Workflow workflow)
			throws IOException, IllegalArgumentException {
		if (!id.equals(workflow.getId())) {
			throw new IllegalArgumentException("Workflow ID does not match Workflow object ID");
		}
		elasticService.index(elasticConfig.getWorkflowIndex(), workflow.getId().toString(), workflow);
		return workflow;
	}
}
