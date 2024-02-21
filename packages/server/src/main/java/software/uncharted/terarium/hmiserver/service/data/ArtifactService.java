package software.uncharted.terarium.hmiserver.service.data;

import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.Artifact;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;

@Service
public class ArtifactService extends S3BackedAssetService<Artifact>{
	public ArtifactService(final ElasticsearchService elasticService, final ElasticsearchConfiguration elasticConfig, final ProjectAssetService projectAssetService, final Config config, final S3ClientService s3ClientService) {
		super(elasticConfig, config, elasticService, projectAssetService, s3ClientService, Artifact.class);
	}

	@Override
	protected String getAssetIndex() {
		return elasticConfig.getArtifactIndex();
	}

	@Override
	protected String getAssetPath() {
		return config.getArtifactPath();
	}
}
