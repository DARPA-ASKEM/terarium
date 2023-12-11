package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.util.List;

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

	public Equation getEquation(String id) throws IOException {
		return elasticService.get(elasticConfig.getEquationIndex(), id, Equation.class);
	}

	public List<Equation> getEquations(Integer page, Integer pageSize) throws IOException {
		final SearchRequest req = new SearchRequest.Builder()
				.index(elasticConfig.getEquationIndex())
				.from(page)
				.size(pageSize)
				.build();
		return elasticService.search(req, Equation.class);
	}

	public void deleteEquation(String id) throws IOException {
		elasticService.delete(elasticConfig.getEquationIndex(), id);
	}

	public Equation createEquation(Equation artifact) throws IOException {
		elasticService.index(elasticConfig.getEquationIndex(), artifact.getId(), artifact);
		return artifact;
	}

	public Equation updateEquation(Equation artifact) throws IOException {
		elasticService.index(elasticConfig.getEquationIndex(), artifact.getId(), artifact);
		return artifact;
	}

}
