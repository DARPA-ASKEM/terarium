package software.uncharted.terarium.hmiserver.service.data;

import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.externalpublication.ExternalPublication;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;

/**
 * Service class for handling external publications.
 */
@Service
public class ExternalPublicationService extends S3BackedAssetService<ExternalPublication>{

		public ExternalPublicationService(final ElasticsearchConfiguration elasticConfig, final Config config, final ElasticsearchService elasticService, final S3ClientService s3ClientService) {
				super(elasticConfig, config, elasticService, s3ClientService, ExternalPublication.class);
		}

		@Override
		protected String getAssetIndex() {
				return elasticConfig.getExternalPublicationIndex();
		}

		@Override
		protected String getAssetPath() {
				return config.getDocumentPath();
		}


}
