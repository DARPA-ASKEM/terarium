package software.uncharted.terarium.hmiserver.service.data;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import io.micrometer.observation.annotation.Observed;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.equation.Equation;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

@Service
@RequiredArgsConstructor
public class EquationService {

	private final ElasticsearchService elasticService;
	private final ElasticsearchConfiguration elasticConfig;

	@Observed(name = "function_profile")
	public Optional<Equation> getAsset(final UUID id) throws IOException {
		final Equation doc = elasticService.get(elasticConfig.getEquationIndex(), id.toString(), Equation.class);
		if (doc != null && doc.getDeletedOn() == null) {
			return Optional.of(doc);
		}
		return Optional.empty();
	}

	@Observed(name = "function_profile")
	public List<Equation> getAssets(final Integer page, final Integer pageSize) throws IOException {
		final SearchRequest req = new SearchRequest.Builder()
				.index(elasticConfig.getEquationIndex())
				.from(page)
				.size(pageSize)
				.query(q -> q.bool(b -> b.mustNot(mn -> mn.exists(e -> e.field("deletedOn")))
						.mustNot(mn -> mn.term(t -> t.field("temporary").value(true)))))
				.build();
		return elasticService.search(req, Equation.class);
	}

	@Observed(name = "function_profile")
	public void deleteAsset(final UUID id) throws IOException {
		final Optional<Equation> equation = getAsset(id);
		if (equation.isEmpty()) {
			return;
		}
		equation.get().setDeletedOn(Timestamp.from(Instant.now()));
		updateAsset(equation.get());
	}

	@Observed(name = "function_profile")
	public Equation createAsset(final Equation equation) throws IOException {
		equation.setCreatedOn(Timestamp.from(Instant.now()));
		elasticService.index(
				elasticConfig.getEquationIndex(),
				equation.setId(UUID.randomUUID()).getId().toString(),
				equation);
		return equation;
	}

	@Observed(name = "function_profile")
	public Optional<Equation> updateAsset(final Equation equation) throws IOException {
		if (!elasticService.documentExists(
				elasticConfig.getEquationIndex(), equation.getId().toString())) {
			return Optional.empty();
		}

		equation.setUpdatedOn(Timestamp.from(Instant.now()));
		elasticService.index(elasticConfig.getEquationIndex(), equation.getId().toString(), equation);
		return Optional.of(equation);
	}
}
