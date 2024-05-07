package software.uncharted.terarium.hmiserver.service.data;

import org.springframework.stereotype.Service;

import io.micrometer.observation.annotation.Observed;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.repository.data.DatasetColumnRepository;
import software.uncharted.terarium.hmiserver.repository.data.DatasetRepository;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;

@Service
public class DatasetService extends TerariumAssetServiceWithSearch<Dataset, DatasetRepository> {

	private final DatasetColumnRepository datasetColumnRepository;

	public DatasetService(
			final Config config,
			final ElasticsearchConfiguration elasticConfig,
			final ElasticsearchService elasticService,
			final ProjectAssetService projectAssetService,
			final S3ClientService s3ClientService,
			final DatasetRepository repository,
			final DatasetColumnRepository datasetColumnRepository) {
		super(config, elasticConfig, elasticService, projectAssetService, s3ClientService, repository, Dataset.class);

		this.datasetColumnRepository = datasetColumnRepository;
	}

	@Override
	@Observed(name = "function_profile")
	protected String getAssetPath() {
		return config.getDatasetPath();
	}

	@Override
	@Observed(name = "function_profile")
	protected String getAssetIndex() {
		return elasticConfig.getDatasetIndex();
	}

	@Override
	public String getAssetAlias() {
		return elasticConfig.getDatasetAlias();
	}
}
