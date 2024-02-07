package software.uncharted.terarium.hmiserver.service.data;

import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.SourceConfig;
import co.elastic.clients.elasticsearch.core.search.SourceFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelDescription;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ModelService extends TerariumAssetService<Model >{

	public ModelService(final ElasticsearchConfiguration elasticConfig, final Config config, final ElasticsearchService elasticService) {
		super(elasticConfig, config, elasticService, Model.class);
	}
	private final ObjectMapper objectMapper = new ObjectMapper();

	public List<ModelDescription> getDescriptions(final Integer page, final Integer pageSize) throws IOException {

		final SourceConfig source = new SourceConfig.Builder()
				.filter(new SourceFilter.Builder().excludes("model", "semantics").build())
				.build();

		final SearchRequest req = new SearchRequest.Builder()
				.index(elasticConfig.getModelIndex())
				.from(page)
				.size(pageSize)
				.query(q -> q.bool(b -> b
					.mustNot(mn -> mn.exists(e -> e.field("deletedOn")))
					.mustNot(mn -> mn.term(t -> t.field("temporary").value(true)))))
				.source(source)
				.build();

		return elasticService.search(req, Model.class).stream().map(m -> ModelDescription.fromModel(m)).toList();
	}

	public Optional<ModelDescription> getDescription(final UUID id) throws IOException {
		final ModelDescription md = ModelDescription
				.fromModel(elasticService.get(elasticConfig.getModelIndex(), id.toString(), Model.class));

		return Optional.of(md);
	}

	public List<Model> searchModels(final Integer page, final Integer pageSize, final JsonNode queryJson) throws IOException {




		Query query = null;
		if (queryJson != null) {
			// if query is provided deserialize it, append the soft delete filter
			final byte[] bytes = objectMapper.writeValueAsString(queryJson).getBytes();
			query = new Query.Builder()
					.bool(b -> b
							.must(new Query.Builder().withJson(
									new ByteArrayInputStream(bytes))
									.build())
							.mustNot(mn -> mn.exists(e -> e.field("deletedOn")))
							.mustNot(mn -> mn.term(t -> t.field("temporary").value(true))))
					.build();
		} else {
			query = new Query.Builder()
					.bool(b -> b
							.mustNot(mn -> mn.exists(e -> e.field("deletedOn")))
							.mustNot(mn -> mn.term(t -> t.field("temporary").value(true))))
					.build();
		}

		final SourceConfig source = new SourceConfig.Builder()
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

	public List<ModelConfiguration> getModelConfigurationsByModelId(final UUID id, final Integer page, final Integer pageSize)
			throws IOException {

		final SearchRequest req = new SearchRequest.Builder()
				.index(elasticConfig.getModelConfigurationIndex())
				.from(page)
				.size(pageSize)
				.query(q -> q
						.bool(b -> b
								.mustNot(mn -> mn.exists(e -> e.field("deletedOn"))) // its ok to return temporary here because we're asking for it by id
								.must(m -> m.term(e -> e.field("modelId").value(id.toString())))))
				.sort(new SortOptions.Builder()
						.field(new FieldSort.Builder().field("timestamp").order(SortOrder.Asc).build()).build())
				.build();

		return elasticService.search(req, ModelConfiguration.class);
	}

	@Override
	protected String getAssetIndex() {
		return elasticConfig.getModelIndex();
	}

	@Override
	public List<Model> getAssets(final Integer page, final Integer pageSize) throws IOException {
		throw new UnsupportedOperationException("Not implemented. Use ModelService.searchModels instead");
	}



}
