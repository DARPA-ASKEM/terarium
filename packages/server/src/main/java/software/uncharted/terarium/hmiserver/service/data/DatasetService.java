package software.uncharted.terarium.hmiserver.service.data;

import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;

@Service
public class DatasetService extends S3BackedAssetService<Dataset>{

	public DatasetService(final ElasticsearchConfiguration elasticConfig, final Config config, final ElasticsearchService elasticService, final ProjectAssetService projectAssetService, final S3ClientService s3ClientService) {
		super(elasticConfig, config, elasticService, projectAssetService, s3ClientService, Dataset.class);
	}
	
	@Override
	protected String getAssetPath() {
		return config.getDatasetPath();
	}

	@Override
	protected String getAssetIndex() {
		return elasticConfig.getDatasetIndex();
	}
}
