package software.uncharted.terarium.hmiserver.service.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.Provenance;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceRelationType;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceType;
import software.uncharted.terarium.hmiserver.service.neo4j.Neo4jService;

@Service
@RequiredArgsConstructor
public class ProvenanceService {

	private final ResourceLoader resourceLoader;

	private final ObjectMapper objectMapper;

	private final Neo4jService neo4jService;

	private Map<String, List<List<String>>> graphValidations;

	@PostConstruct
	public void init() throws Exception {
		graphValidations = loadGraphValidations();
	}

	@Observed(name = "function_profile")
	public Map<String, List<List<String>>> loadGraphValidations() throws Exception {
		final Resource resource = resourceLoader.getResource("classpath:graph_relations.json");
		final InputStream inputStream = resource.getInputStream();
		final Map<String, List<List<String>>> jsonMap = objectMapper.readValue(
			new InputStreamReader(inputStream),
			new TypeReference<Map<String, List<List<String>>>>() {}
		);
		return jsonMap;
	}

	private boolean validateRelationship(
		final ProvenanceType left,
		final ProvenanceType right,
		final ProvenanceRelationType relationType
	) {
		if (left == null || right == null || relationType == null) {
			return false;
		}
		final List<List<String>> relationshipAllowedTypes = graphValidations.get(relationType.toString());
		if (relationshipAllowedTypes == null) {
			return false;
		}
		for (final List<String> relation : relationshipAllowedTypes) {
			final ProvenanceType expectedLeft = ProvenanceType.findByType(relation.get(0));
			final ProvenanceType expectedRight = ProvenanceType.findByType(relation.get(1));

			if (left == expectedLeft && right == expectedRight) {
				return true;
			}
		}
		return false;
	}

	@Observed(name = "function_profile")
	public Provenance createProvenance(final Provenance provenance) {
		if (!validateRelationship(provenance.getLeftType(), provenance.getRightType(), provenance.getRelationType())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid relationship");
		}

		try (final Session session = neo4jService.getSession()) {
			// if node 1 is not created yet create node
			final String leftNodeQuery = String.format(
				"MERGE (n:%s {id: '%s', concept: '%s'})",
				provenance.getLeftType(),
				provenance.getLeft().toString(),
				provenance.getConcept() != null ? provenance.getConcept() : "."
			);
			session.run(leftNodeQuery);

			// if node 2 is not created yet create node
			final String rightNodeQuery = String.format(
				"MERGE (n:%s {id: '%s', concept: '.'})",
				provenance.getRightType(),
				provenance.getRight().toString()
			);
			session.run(rightNodeQuery);

			// create edge
			final String edgeQuery = String.format(
				"MATCH (n1:%s {id: $left_id}) MATCH (n2:%s {id: $right_id}) MERGE (n1)-[:%s {user_id: $user_id}]->(n2)",
				provenance.getLeftType(),
				provenance.getRightType(),
				provenance.getRelationType()
			);
			session.run(
				edgeQuery,
				Values.parameters(
					"left_id",
					provenance.getLeft().toString(),
					"right_id",
					provenance.getRight().toString(),
					"user_id",
					provenance.getUserId() != null ? provenance.getUserId() : ""
				)
			);
		}

		return provenance;
	}

	@Observed(name = "function_profile")
	public void deleteHangingNodes() {
		try (final Session session = neo4jService.getSession()) {
			final String query = "MATCH (n) WHERE NOT (n)--() DELETE n";
			session.run(query);
		}
	}
}
