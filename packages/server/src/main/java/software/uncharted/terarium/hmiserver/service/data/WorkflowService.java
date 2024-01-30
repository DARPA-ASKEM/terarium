package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import lombok.RequiredArgsConstructor;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.Workflow;
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
				.query(q -> q.bool(b -> b
					.mustNot(mn -> mn.exists(e -> e.field("deletedOn")))
					.mustNot(mn -> mn.term(t -> t.field("temporary").value(true)))))
				.build();
		return elasticService.search(req, Workflow.class);
	}

	public Optional<Workflow> getWorkflow(final UUID id) throws IOException {
		Workflow doc = elasticService.get(elasticConfig.getWorkflowIndex(), id.toString(), Workflow.class);
		if (doc != null && doc.getDeletedOn() == null) {
			return Optional.of(doc);
		}
		return Optional.empty();
	}

	public void deleteWorkflow(final UUID id) throws IOException {
		Optional<Workflow> workflow = getWorkflow(id);
		if (workflow.isEmpty()) {
			return;
		}
		workflow.get().setDeletedOn(Timestamp.from(Instant.now()));
		updateWorkflow(workflow.get());
	}

	public Workflow createWorkflow(final Workflow workflow) throws IOException {
		elasticService.index(elasticConfig.getWorkflowIndex(), workflow.setId(UUID.randomUUID()).getId().toString(),
				workflow);
		return workflow;
	}

	public Optional<Workflow> updateWorkflow(final Workflow workflow) throws IOException {
		if (!elasticService.contains(elasticConfig.getWorkflowIndex(), workflow.getId().toString())) {
			return Optional.empty();
		}
		workflow.setUpdatedOn(Timestamp.from(Instant.now()));
		elasticService.index(elasticConfig.getWorkflowIndex(), workflow.getId().toString(), workflow);
		return Optional.of(workflow);
	}
}
