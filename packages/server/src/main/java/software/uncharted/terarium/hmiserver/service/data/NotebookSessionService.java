package software.uncharted.terarium.hmiserver.service.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.models.dataservice.notebooksession.NotebookSession;
import software.uncharted.terarium.hmiserver.repository.data.NotebookSessionRepository;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;

@Service
public class NotebookSessionService
		extends TerariumAssetServiceWithoutSearch<NotebookSession, NotebookSessionRepository> {

	public NotebookSessionService(
			final ObjectMapper objectMapper,
			final Config config,
			final ProjectAssetService projectAssetService,
			final NotebookSessionRepository repository,
			final S3ClientService s3ClientService) {
		super(objectMapper, config, projectAssetService, repository, s3ClientService, NotebookSession.class);
	}

	@Override
	protected String getAssetPath() {
		throw new UnsupportedOperationException("Notebook Sessions are not stored in S3");
	}
}
