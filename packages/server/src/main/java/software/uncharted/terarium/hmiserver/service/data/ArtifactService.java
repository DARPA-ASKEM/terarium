package software.uncharted.terarium.hmiserver.service.data;

import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.Artifact;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;

@Service
public class ArtifactService extends S3BackedAssetService<Artifact> {
	public ArtifactService(
			final ElasticsearchConfiguration elasticConfig,
			final Config config,
			final ElasticsearchService elasticService,
			final ProjectAssetService projectAssetService,
			final S3ClientService s3ClientService) {
		super(elasticConfig, config, elasticService, projectAssetService, s3ClientService, Artifact.class);
	}

	@Override
	@Observed(name = "function_profile")
	protected String getAssetIndex() {
		return elasticConfig.getArtifactIndex();
	}

	@Override
	@Observed(name = "function_profile")
	protected String getAssetPath() {
		return config.getArtifactPath();
	}
}
