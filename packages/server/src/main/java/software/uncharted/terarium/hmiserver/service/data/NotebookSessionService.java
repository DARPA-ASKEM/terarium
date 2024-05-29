package software.uncharted.terarium.hmiserver.service.data;

import io.micrometer.observation.annotation.Observed;
import java.io.IOException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.notebooksession.NotebookSession;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@Service
public class NotebookSessionService extends TerariumAssetService<NotebookSession> {

	public NotebookSessionService(
			final ElasticsearchConfiguration elasticConfig,
			final Config config,
			final ElasticsearchService elasticService,
			final ProjectAssetService projectAssetService) {
		super(elasticConfig, config, elasticService, projectAssetService, NotebookSession.class);
	}

	@Override
	@Observed(name = "function_profile")
	protected String getAssetIndex() {
		return elasticConfig.getNotebookSessionIndex();
	}

	@Override
	public NotebookSession cloneAndPersistAsset(final UUID id, final Schema.Permission hasWritePermission)
			throws IOException {
		throw new UnsupportedOperationException("Unimplemented");
	}
}
