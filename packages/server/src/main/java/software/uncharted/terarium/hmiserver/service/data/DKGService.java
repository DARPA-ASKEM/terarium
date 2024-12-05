package software.uncharted.terarium.hmiserver.service.data;

import co.elastic.clients.elasticsearch._types.KnnQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.MsearchResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.msearch.MultisearchBody;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.SourceConfig;
import io.micrometer.observation.annotation.Observed;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.TerariumAssetEmbeddings;
import software.uncharted.terarium.hmiserver.models.TerariumAssetEmbeddings.Embedding;
import software.uncharted.terarium.hmiserver.models.mira.DKG;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.gollm.EmbeddingService;

@Service
@RequiredArgsConstructor
@Slf4j
public class DKGService {

	private final ElasticsearchConfiguration elasticConfig;
	private final ElasticsearchService elasticService;
	private final EmbeddingService embeddingService;
	private static final int EMPHASIS = 2;

	/**
	 * Search for DKG entities using embeddings in the EpiDKG index
	 * @param page page number
	 * @param pageSize page size
	 * @param k number of nearest neighbors
	 * @param texts text to search
	 * @param source source config
	 * @return list of DKG entities
	 * @throws IOException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	@Observed(name = "function_profile")
	public List<DKG> knnSearchEpiDKG(
		final Integer page,
		final Integer pageSize,
		final Integer k,
		final List<String> texts,
		final SourceConfig source
	) throws IOException, ExecutionException, InterruptedException, TimeoutException {
		MsearchResponse<DKG> results = knnSearchDKG(elasticConfig.getEpiDKGIndex(), page, pageSize, k, texts, source);

		// return the result with the highest score
		return results != null
			? results
				.responses()
				.stream()
				.flatMap(item -> item.result().hits().hits().stream())
				.map(Hit::source)
				.collect(Collectors.toList())
			: null;
	}

	/**
	 * Search for DKG entities using embeddings in the ClimateDKG index
	 * @param page page number
	 * @param pageSize page size
	 * @param k number of nearest neighbors
	 * @param texts text to search
	 * @param source source config
	 * @return list of DKG entities
	 * @throws IOException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	@Observed(name = "function_profile")
	public List<DKG> knnSearchClimateDKG(
		final Integer page,
		final Integer pageSize,
		final Integer k,
		final List<String> texts,
		final SourceConfig source
	) throws IOException, ExecutionException, InterruptedException, TimeoutException {
		MsearchResponse<DKG> results = knnSearchDKG(elasticConfig.getClimateDKGIndex(), page, pageSize, k, texts, source);

		// transform the results into a list of DKG entities
		return results != null
			? results
				.responses()
				.stream()
				.flatMap(item -> item.result().hits().hits().stream())
				.map(Hit::source)
				.collect(Collectors.toList())
			: null;
	}

	/**
	 * Search for DKG entities
	 * @param page page number
	 * @param pageSize page size
	 * @param q query string
	 * @param source source config
	 * @return list of DKG entities
	 * @throws IOException
	 */
	@Observed(name = "function_profile")
	public List<DKG> searchEpiDKG(final Integer page, final Integer pageSize, final String q, final SourceConfig source)
		throws IOException {
		return searchDKG(elasticConfig.getEpiDKGIndex(), page, pageSize, q, source);
	}

	/**
	 * Search for DKG entities in the ClimateDKG index
	 * @param page page number
	 * @param pageSize page size
	 * @param q query string
	 * @param source source config
	 * @return list of DKG entities
	 * @throws IOException
	 */
	@Observed(name = "function_profile")
	public List<DKG> searchClimateDKG(
		final Integer page,
		final Integer pageSize,
		final String q,
		final SourceConfig source
	) throws IOException {
		return searchDKG(elasticConfig.getClimateDKGIndex(), page, pageSize, q, source);
	}

	/**
	 * Get a DKG entity by curie in the EpiDKG index
	 * @param curie curie of the entity
	 * @return list of DKG entities
	 * @throws IOException
	 */
	@Observed(name = "function_profile")
	public List<DKG> getEpiEntity(final String curie) throws IOException {
		return getEntity(elasticConfig.getEpiDKGIndex(), curie);
	}

	/**
	 * Get a DKG entity by curie in the ClimateDKG index
	 * @param curie curie of the entity
	 * @return list of DKG entities
	 * @throws IOException
	 */
	@Observed(name = "function_profile")
	public List<DKG> getClimateEntity(final String curie) throws IOException {
		return getEntity(elasticConfig.getClimateDKGIndex(), curie);
	}

	@Observed(name = "function_profile")
	private MsearchResponse<DKG> knnSearchDKG(
		final String index,
		final Integer page,
		final Integer pageSize,
		final Integer k,
		final List<String> texts,
		final SourceConfig source
	) throws IOException, ExecutionException, InterruptedException, TimeoutException {
		if (k > pageSize) {
			return null; //error case
		}

		final MultisearchBody.Builder msBuilder = new MultisearchBody.Builder();

		final List<KnnQuery> queries = new ArrayList<>();
		if (texts != null && !texts.isEmpty()) {
			// remove null or empty strings from the list
			final List<String> embeddingTexts = texts
				.stream()
				.filter(text -> text != null && !text.isEmpty())
				.collect(Collectors.toList());

			final TerariumAssetEmbeddings embeddings = embeddingService.generateEmbeddings(embeddingTexts);

			for (Embedding embedding : embeddings.getEmbeddings()) {
				final List<Float> vectors = Arrays.stream(embedding.getVector())
					.mapToObj(d -> (float) d)
					.collect(Collectors.toList());

				queries.add(
					new KnnQuery.Builder().field(DKG.EMBEDDINGS).queryVector(vectors).k(k).numCandidates(pageSize).build()
				);
			}
		}

		return elasticService.bulkKnnSearch(index, queries, DKG.class);
	}

	@Observed(name = "function_profile")
	private List<DKG> searchDKG(
		final String index,
		final Integer page,
		final Integer pageSize,
		final String q,
		final SourceConfig source
	) throws IOException {
		final SearchRequest.Builder builder = new SearchRequest.Builder().index(index).from(page).size(pageSize);

		if (q != null && !q.isEmpty()) {
			Query query = QueryBuilders.multiMatch()
				.fields(ElasticsearchService.emphasis(DKG.NAME, EMPHASIS), DKG.DESCRIPTION)
				.query(q)
				.build()
				._toQuery();
			builder.query(query);
		}

		if (source != null) {
			builder.source(source);
		}

		final SearchRequest req = builder.build();
		return elasticService.search(req, DKG.class);
	}

	private List<DKG> getEntity(final String index, final String curie) throws IOException {
		Query query = QueryBuilders.matchPhrase().field(DKG.ID).query(curie).build()._toQuery();

		final SearchRequest req = new SearchRequest.Builder().index(index).from(0).size(10).query(query).build();
		return elasticService.search(req, DKG.class);
	}
}
