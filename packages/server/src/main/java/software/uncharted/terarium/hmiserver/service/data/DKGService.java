package software.uncharted.terarium.hmiserver.service.data;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.SourceConfig;
import io.micrometer.observation.annotation.Observed;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.mira.DKG;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

@Service
@RequiredArgsConstructor
public class DKGService {

	private final ElasticsearchConfiguration elasticConfig;
	private final ElasticsearchService elasticService;
	private static final String EMPHASIS = "^2";

	@Observed(name = "function_profile")
	public List<DKG> searchEpiDKG(final Integer page, final Integer pageSize, final String q, final SourceConfig source)
		throws IOException {
		return searchDKG(elasticConfig.getEpiDKGIndex(), page, pageSize, q, source);
	}

	@Observed(name = "function_profile")
	public List<DKG> searchClimateDKG(
		final Integer page,
		final Integer pageSize,
		final String q,
		final SourceConfig source
	) throws IOException {
		return searchDKG(elasticConfig.getClimateDKGIndex(), page, pageSize, q, source);
	}

	private List<DKG> searchDKG(
		final String index,
		final Integer page,
		final Integer pageSize,
		final String q,
		final SourceConfig source
	) throws IOException {
		final SearchRequest.Builder builder = new SearchRequest.Builder().index(index).from(page).size(pageSize);

		if (q != null && !q.isEmpty()) {
			Query query = QueryBuilders.multiMatch().fields(DKG.NAME + EMPHASIS, DKG.DESCRIPTION).query(q).build()._toQuery();
			builder.query(query);
		}

		if (source != null) {
			builder.source(source);
		}

		final SearchRequest req = builder.build();
		return elasticService.search(req, DKG.class);
	}

	@Observed(name = "function_profile")
	public List<DKG> getEpiEntity(final String curie) throws IOException {
		return getEntity(elasticConfig.getEpiDKGIndex(), curie);
	}

	@Observed(name = "function_profile")
	public List<DKG> getClimateEntity(final String curie) throws IOException {
		return getEntity(elasticConfig.getClimateDKGIndex(), curie);
	}

	private List<DKG> getEntity(final String index, final String curie) throws IOException {
		Query query = QueryBuilders.matchPhrase().field(DKG.ID).query(curie).build()._toQuery();

		final SearchRequest req = new SearchRequest.Builder().index(index).from(0).size(10).query(query).build();
		return elasticService.search(req, DKG.class);
	}
}
