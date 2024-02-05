package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import lombok.RequiredArgsConstructor;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.multiphysics.DecapodesContext;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

@Service
@RequiredArgsConstructor
public class DecapodesContextService {

	private final ElasticsearchService elasticService;
	private final ElasticsearchConfiguration elasticConfig;

	public List<DecapodesContext> getDecapodesContexts(Integer page, Integer pageSize)
			throws IOException {

		final SearchRequest req = new SearchRequest.Builder()
				.index(elasticConfig.getDecapodesContextIndex())
				.size(pageSize)
				.query(q -> q.bool(b -> b.mustNot(mn -> mn.exists(e -> e.field("deletedOn")))))
				.sort(new SortOptions.Builder()
						.field(new FieldSort.Builder().field("timestamp").order(SortOrder.Asc).build()).build())
				.build();

		return elasticService.search(req, DecapodesContext.class);
	}

	public Optional<DecapodesContext> getDecapodesContext(UUID id) throws IOException {
		DecapodesContext doc = elasticService.get(elasticConfig.getDecapodesContextIndex(), id.toString(),
				DecapodesContext.class);
		if (doc != null && doc.getDeletedOn() == null) {
			return Optional.of(doc);
		}
		return Optional.empty();
	}

	public void deleteDecapodesContext(UUID id) throws IOException {
		Optional<DecapodesContext> decapodesContext = getDecapodesContext(id);
		if (decapodesContext.isEmpty()) {
			return;
		}
		decapodesContext.get().setDeletedOn(Timestamp.from(Instant.now()));
		updateDecapodesContext(decapodesContext.get());
	}

	public DecapodesContext createDecapodesContext(DecapodesContext decapodesContext) throws IOException {
		decapodesContext.setCreatedOn(Timestamp.from(Instant.now()));
		elasticService.index(elasticConfig.getDecapodesContextIndex(),
				decapodesContext.setId(UUID.randomUUID()).getId().toString(),
				decapodesContext);
		return decapodesContext;
	}

	public Optional<DecapodesContext> updateDecapodesContext(DecapodesContext decapodesContext)
			throws IOException {
		if (!elasticService.contains(elasticConfig.getDecapodesContextIndex(),
				decapodesContext.getId().toString())) {
			return Optional.empty();
		}
		decapodesContext.setUpdatedOn(Timestamp.from(Instant.now()));
		elasticService.index(elasticConfig.getDecapodesContextIndex(), decapodesContext.getId().toString(),
				decapodesContext);
		return Optional.of(decapodesContext);
	}

}
