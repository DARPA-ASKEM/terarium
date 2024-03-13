package software.uncharted.terarium.hmiserver.service.data;

import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelConfiguration;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

@Service

public class ModelConfigurationService extends TerariumAssetService<ModelConfiguration> {

	public ModelConfigurationService(final ElasticsearchConfiguration elasticConfig, final Config config, final ElasticsearchService elasticService, final ProjectAssetService projectAssetService) {
		super(elasticConfig, config, elasticService, projectAssetService, ModelConfiguration.class);
	}

	@Override
	protected String getAssetIndex() {
		return elasticConfig.getModelConfigurationIndex();
	}

}
