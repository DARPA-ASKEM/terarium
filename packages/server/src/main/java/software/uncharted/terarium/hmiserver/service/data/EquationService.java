package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import lombok.RequiredArgsConstructor;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.equation.Equation;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

@Service
@RequiredArgsConstructor
public class EquationService {

	private final ElasticsearchService elasticService;
	private final ElasticsearchConfiguration elasticConfig;

	public Equation getEquation(UUID id) throws IOException {
		return elasticService.get(elasticConfig.getEquationIndex(), id.toString(), Equation.class);
	}

	public List<Equation> getEquations(Integer page, Integer pageSize) throws IOException {
		final SearchRequest req = new SearchRequest.Builder()
				.index(elasticConfig.getEquationIndex())
				.from(page)
				.size(pageSize)
				.query(q -> q.bool(b -> b.mustNot(mn-> mn.exists(e->e.field("deleted_on")))))
				.build();
		return elasticService.search(req, Equation.class);
	}

	public void deleteEquation(UUID id) throws IOException {

		Equation equation = getEquation(id);
		equation.setDeletedOn(Timestamp.from(Instant.now()));
		updateEquation(equation);
	}

	public Equation createEquation(Equation equation) throws IOException {
		equation.setCreatedOn(Timestamp.from(Instant.now()));
		elasticService.index(elasticConfig.getEquationIndex(), equation.setId(UUID.randomUUID()).getId().toString(),
				equation);
		return equation;
	}

	public Optional<Equation> updateEquation(Equation equation) throws IOException {
		if (!elasticService.contains(elasticConfig.getEquationIndex(), equation.getId().toString())) {
			return Optional.empty();
		}

		equation.setUpdatedOn(Timestamp.from(Instant.now()));
		elasticService.index(elasticConfig.getEquationIndex(), equation.getId().toString(), equation);
		return Optional.of(equation);
	}

}
