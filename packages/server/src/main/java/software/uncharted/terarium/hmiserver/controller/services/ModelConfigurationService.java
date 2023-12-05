package software.uncharted.terarium.hmiserver.controller.services;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import lombok.RequiredArgsConstructor;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelConfiguration;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

@Service
@RequiredArgsConstructor
public class ModelConfigurationService {

	private final ElasticsearchService elasticService;
	private final ElasticsearchConfiguration elasticConfig;

	public List<ModelConfiguration> getModelConfigurations(Integer page, Integer pageSize)
			throws IOException {

		final SearchRequest req = new SearchRequest.Builder()
				.index(elasticConfig.getModelConfigurationIndex())
				.size(pageSize)
				.sort(new SortOptions.Builder()
						.field(new FieldSort.Builder().field("timestamp").order(SortOrder.Asc).build()).build())
				.build();

		return elasticService.search(req, ModelConfiguration.class);
	}

	public ModelConfiguration getModelConfiguration(String id) throws IOException {
		return elasticService.get(elasticConfig.getModelConfigurationIndex(), id, ModelConfiguration.class);
	}

	public void deleteModelConfiguration(String id) throws IOException {
		elasticService.delete(elasticConfig.getModelConfigurationIndex(), id);
	}

	public ModelConfiguration createModelConfiguration(ModelConfiguration modelConfiguration) throws IOException {
		elasticService.index(elasticConfig.getModelConfigurationIndex(), modelConfiguration.getId(),
				modelConfiguration);
		return modelConfiguration;
	}

	public ModelConfiguration updateModelConfiguration(ModelConfiguration modelConfiguration) throws IOException {
		elasticService.index(elasticConfig.getModelConfigurationIndex(), modelConfiguration.getId(),
				modelConfiguration);
		return modelConfiguration;
	}

}
