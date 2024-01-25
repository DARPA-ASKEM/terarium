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
import software.uncharted.terarium.hmiserver.models.dataservice.notebooksession.NotebookSession;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

@Service
@RequiredArgsConstructor
public class NotebookSessionService {

	private final ElasticsearchService elasticService;
	private final ElasticsearchConfiguration elasticConfig;

	public Optional<NotebookSession> getNotebookSession(UUID id) throws IOException {
		NotebookSession doc = elasticService.get(elasticConfig.getNotebookSessionIndex(), id.toString(),
				NotebookSession.class);
		if (doc != null && doc.getDeletedOn() == null) {
			return Optional.of(doc);
		}
		return Optional.empty();
	}

	public List<NotebookSession> getNotebookSessions(Integer page, Integer pageSize) throws IOException {
		final SearchRequest req = new SearchRequest.Builder()
				.index(elasticConfig.getNotebookSessionIndex())
				.from(page)
				.size(pageSize)
				.query(q -> q.bool(b -> b.mustNot(mn -> mn.exists(e -> e.field("deletedOn")))))
				.build();
		return elasticService.search(req, NotebookSession.class);
	}

	public void deleteNotebookSession(UUID id) throws IOException {

		Optional<NotebookSession> notebookSession = getNotebookSession(id);
		if (notebookSession.isEmpty()) {
			return;
		}
		notebookSession.get().setDeletedOn(Timestamp.from(Instant.now()));
		updateNotebookSession(notebookSession.get());
	}

	public NotebookSession createNotebookSession(NotebookSession notebookSession) throws IOException {
		notebookSession.setCreatedOn(Timestamp.from(Instant.now()));
		elasticService.index(elasticConfig.getNotebookSessionIndex(),
				notebookSession.setId(UUID.randomUUID()).getId().toString(),
				notebookSession);
		return notebookSession;
	}

	public Optional<NotebookSession> updateNotebookSession(NotebookSession notebookSession) throws IOException {
		if (!elasticService.contains(elasticConfig.getNotebookSessionIndex(), notebookSession.getId().toString())) {
			return Optional.empty();
		}
		notebookSession.setUpdatedOn(Timestamp.from(Instant.now()));
		elasticService.index(elasticConfig.getNotebookSessionIndex(), notebookSession.getId().toString(),
				notebookSession);
		return Optional.of(notebookSession);
	}

}
