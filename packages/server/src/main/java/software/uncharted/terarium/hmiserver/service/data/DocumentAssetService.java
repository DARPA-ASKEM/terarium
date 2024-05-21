package software.uncharted.terarium.hmiserver.service.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.repository.data.DocumentRepository;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;

@Service
public class DocumentAssetService extends TerariumAssetServiceWithSearch<DocumentAsset, DocumentRepository> {

	public DocumentAssetService(
			final ObjectMapper objectMapper,
			final Config config,
			final ElasticsearchConfiguration elasticConfig,
			final ElasticsearchService elasticService,
			final ProjectAssetService projectAssetService,
			final S3ClientService s3ClientService,
			final DocumentRepository repository) {
		super(
				objectMapper,
				config,
				elasticConfig,
				elasticService,
				projectAssetService,
				s3ClientService,
				repository,
				DocumentAsset.class);
	}

	@Override
	@Observed(name = "function_profile")
	protected String getAssetPath() {
		return config.getDocumentPath();
	}

	@Override
	@Observed(name = "function_profile")
	protected String getAssetIndex() {
		return elasticConfig.getDocumentIndex();
	}

	@Override
	public String getAssetAlias() {
		return elasticConfig.getDocumentAlias();
	}
}
