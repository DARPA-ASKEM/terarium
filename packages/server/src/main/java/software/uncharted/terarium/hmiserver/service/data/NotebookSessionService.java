package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.apache.http.entity.ContentType;
import org.springframework.stereotype.Service;

import io.micrometer.observation.annotation.Observed;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.FileExport;
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

	@Observed(name = "function_profile")
	public void copyAssetFiles(final NotebookSession newAsset, final NotebookSession oldAsset,
			final Schema.Permission hasWritePermission)
			throws IOException {

		throw new UnsupportedOperationException("Unimplemented");
	}

	@Observed(name = "function_profile")
	public Map<String, FileExport> exportAssetFiles(final UUID assetId, final Schema.Permission hasReadPermission) {

		throw new UnsupportedOperationException("Unimplemented");
	}

	public Integer uploadFile(final UUID uuid, final String filename, final ContentType contentType, final byte[] data)
			throws IOException {
		throw new UnsupportedOperationException("Unimplemented");
	}
}
