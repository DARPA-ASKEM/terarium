package software.uncharted.terarium.hmiserver.service.data;

import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;

@Service
public class DocumentAssetService extends S3BackedAssetService<DocumentAsset> {


	public DocumentAssetService(final ElasticsearchConfiguration elasticConfig, final Config config, final ElasticsearchService elasticService, final S3ClientService s3ClientService) {
		super(elasticConfig, config, elasticService, s3ClientService, DocumentAsset.class);
	}

	@Override
	protected String getAssetPath() {
		return config.getDocumentPath();
	}

	@Override
	protected String getAssetIndex() {
		return elasticConfig.getDocumentIndex();
	}

}
