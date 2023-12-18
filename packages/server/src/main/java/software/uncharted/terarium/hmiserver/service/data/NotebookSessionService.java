package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
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

	public NotebookSession getNotebookSession(UUID id) throws IOException {
		return elasticService.get(elasticConfig.getNotebookSessionIndex(), id.toString(), NotebookSession.class);
	}

	public List<NotebookSession> getNotebookSessions(Integer page, Integer pageSize) throws IOException {
		final SearchRequest req = new SearchRequest.Builder()
				.index(elasticConfig.getNotebookSessionIndex())
				.from(page)
				.size(pageSize)
				.build();
		return elasticService.search(req, NotebookSession.class);
	}

	public void deleteNotebookSession(UUID id) throws IOException {
		elasticService.delete(elasticConfig.getNotebookSessionIndex(), id.toString());
	}

	public NotebookSession createNotebookSession(NotebookSession notebookSession) throws IOException {
		notebookSession.setCreatedOn(Timestamp.from(Instant.now()));
		elasticService.index(elasticConfig.getNotebookSessionIndex(),
				notebookSession.setId(UUID.randomUUID()).getId().toString(),
				notebookSession);
		return notebookSession;
	}

	public NotebookSession updateNotebookSession(NotebookSession notebookSession) throws IOException {
		notebookSession.setUpdatedOn(Timestamp.from(Instant.now()));
		elasticService.index(elasticConfig.getNotebookSessionIndex(), notebookSession.getId().toString(),
				notebookSession);
		return notebookSession;
	}

}
