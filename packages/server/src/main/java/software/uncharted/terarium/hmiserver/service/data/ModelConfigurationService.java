package software.uncharted.terarium.hmiserver.service.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.models.dataservice.model.configurations.ModelConfiguration;
import software.uncharted.terarium.hmiserver.repository.data.ModelConfigRepository;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;

@Service
public class ModelConfigurationService
		extends TerariumAssetServiceWithoutSearch<ModelConfiguration, ModelConfigRepository> {
	public ModelConfigurationService(
			final ObjectMapper objectMapper,
			final Config config,
			final ProjectAssetService projectAssetService,
			final ModelConfigRepository repository,
			final S3ClientService s3ClientService) {
		super(objectMapper, config, projectAssetService, repository, s3ClientService, ModelConfiguration.class);
	}

	@Override
	protected String getAssetPath() {
		throw new UnsupportedOperationException("Model Configurations are not stored in S3");
	}
}
