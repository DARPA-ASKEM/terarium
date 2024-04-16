package software.uncharted.terarium.hmiserver.service.data;

import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.notebooksession.NotebookSession;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

@Service
public class NotebookSessionService extends TerariumAssetService<NotebookSession> {

    public NotebookSessionService(
            final ElasticsearchConfiguration elasticConfig,
            final Config config,
            final ElasticsearchService elasticService,
            final ProjectAssetService projectAssetService) {
        super(elasticConfig, config, elasticService, projectAssetService, NotebookSession.class);
    }

    @Override
    protected String getAssetIndex() {
        return elasticConfig.getNotebookSessionIndex();
    }
}
