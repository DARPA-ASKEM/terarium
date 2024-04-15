package software.uncharted.terarium.hmiserver.service.data;

import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.code.Code;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;

@Service
public class CodeService extends S3BackedAssetService<Code> {

    public CodeService(
            final ElasticsearchConfiguration elasticConfig,
            final Config config,
            final ElasticsearchService elasticService,
            final ProjectAssetService projectAssetService,
            final S3ClientService s3ClientService) {
        super(elasticConfig, config, elasticService, projectAssetService, s3ClientService, Code.class);
    }

    @Override
    protected String getAssetIndex() {
        return elasticConfig.getCodeIndex();
    }

    @Override
    protected String getAssetPath() {
        return config.getCodePath();
    }
}
