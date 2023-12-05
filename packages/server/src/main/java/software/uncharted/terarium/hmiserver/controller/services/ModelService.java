package software.uncharted.terarium.hmiserver.controller.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.SourceConfig;
import co.elastic.clients.elasticsearch.core.search.SourceFilter;
import lombok.RequiredArgsConstructor;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelDescription;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

@Service
@RequiredArgsConstructor
public class ModelService {

	private final ElasticsearchService elasticService;
	private final ElasticsearchConfiguration elasticConfig;
	private final ObjectMapper objectMapper;

	public List<ModelDescription> getDescriptions(Integer page, Integer pageSize) throws IOException {

		SourceConfig source = new SourceConfig.Builder()
				.filter(new SourceFilter.Builder().excludes("model", "semantics").build())
				.build();

		final SearchRequest req = new SearchRequest.Builder()
				.index(elasticConfig.getModelIndex())
				.from(page)
				.size(pageSize)
				.source(source)
				.build();

		return elasticService.search(req, Model.class).stream().map(m -> ModelDescription.fromModel(m)).toList();
	}

	public ModelDescription getDescription(String id) throws IOException {
		return ModelDescription.fromModel(elasticService.get(elasticConfig.getModelIndex(), id, Model.class));
	}

	public List<Model> searchModels(Integer page, Integer pageSize, JsonNode queryJson) throws IOException {

		Query query = null;
		if (queryJson != null) {
			query = new Query.Builder().withJson(
					new ByteArrayInputStream(objectMapper.writeValueAsString(queryJson).getBytes())).build();
		}

		SourceConfig source = new SourceConfig.Builder()
				.filter(new SourceFilter.Builder().excludes("model", "semantics").build())
				.build();

		final SearchRequest req = new SearchRequest.Builder()
				.index(elasticConfig.getModelIndex())
				.from(page)
				.size(pageSize)
				.source(source)
				.query(query)
				.build();
		return elasticService.search(req, Model.class);
	}

	public List<ModelConfiguration> getModelConfigurationsByModelId(String id, Integer page, Integer pageSize)
			throws IOException {

		final SearchRequest req = new SearchRequest.Builder()
				.index(elasticConfig.getModelConfigurationIndex())
				.size(pageSize)
				.query(new Query.Builder().term(new TermQuery.Builder().field("model_id").value(id).build()).build())
				.sort(new SortOptions.Builder()
						.field(new FieldSort.Builder().field("timestamp").order(SortOrder.Asc).build()).build())
				.build();

		return elasticService.search(req, ModelConfiguration.class);
	}

	public Model getModel(String id) throws IOException {
		return elasticService.get(elasticConfig.getModelIndex(), id, Model.class);
	}

	public void deleteModel(String id) throws IOException {
		elasticService.delete(elasticConfig.getModelIndex(), id);
	}

	public Model createModel(Model model) throws IOException {
		elasticService.index(elasticConfig.getModelIndex(), model.getId(), model);
		return model;
	}

	public Model updateModel(Model model) throws IOException {
		elasticService.index(elasticConfig.getModelIndex(), model.getId(), model);
		return model;
	}

}
