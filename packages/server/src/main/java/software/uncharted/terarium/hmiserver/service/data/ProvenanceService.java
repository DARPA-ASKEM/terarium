package software.uncharted.terarium.hmiserver.service.data;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.Provenance;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceRelationType;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceType;
import software.uncharted.terarium.hmiserver.repository.data.ProvenanceRepository;
import software.uncharted.terarium.hmiserver.service.neo4j.Neo4jService;

@Service
@RequiredArgsConstructor
public class ProvenanceService {

	final private ResourceLoader resourceLoader;

	final private ObjectMapper objectMapper;

	final private Neo4jService neo4jService;

	final ProvenanceRepository provenanceRepository;

	private Map<String, List<List<String>>> graphValidations;

	@PostConstruct
	public void init() throws Exception {
		graphValidations = loadGraphValidations();
	}

	public Map<String, List<List<String>>> loadGraphValidations() throws Exception {
		Resource resource = resourceLoader.getResource("classpath:graph_relations.json");
		InputStream inputStream = resource.getInputStream();
		Map<String, List<List<String>>> jsonMap = objectMapper.readValue(new InputStreamReader(inputStream), Map.class);
		return jsonMap;
	}

	private boolean validateRelationship(ProvenanceType left, ProvenanceType right,
			ProvenanceRelationType relationType) {
		if (left == null || right == null || relationType == null) {
			return false;
		}
		List<List<String>> relationshipAllowedTypes = graphValidations.get(relationType.toString());
		if (relationshipAllowedTypes == null) {
			return false;
		}
		for (List<String> relation : relationshipAllowedTypes) {

			ProvenanceType expectedLeft = ProvenanceType.findByType(relation.get(0));
			ProvenanceType expectedRight = ProvenanceType.findByType(relation.get(1));

			if (left == expectedLeft && right == expectedRight) {
				return true;
			}
		}
		return false;
	}

	public Provenance createProvenance(Provenance provenance) {
		if (!validateRelationship(provenance.getLeftType(), provenance.getRightType(), provenance.getRelationType())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid relationship");
		}

		provenanceRepository.save(provenance);

		try (Session session = neo4jService.getSession()) {
			// if node 1 is not created yet create node
			String leftNodeQuery = String.format(
					"MERGE (n:%s {id: '%s', concept: '%s'})",
					provenance.getLeftType(),
					provenance.getLeft().toString(),
					provenance.getConcept() != null ? provenance.getConcept() : ".");
			session.run(leftNodeQuery);

			// if node 2 is not created yet create node
			String rightNodeQuery = String.format(
					"MERGE (n:%s {id: '%s', concept: '.'})",
					provenance.getRightType(),
					provenance.getRight().toString());
			session.run(rightNodeQuery);

			// create edge
			String edgeQuery = String.format(
					"MATCH (n1:%s {id: $left_id}) MATCH (n2:%s {id: $right_id}) MERGE (n1)-[:%s {user_id: $user_id, provenance_id: $provenance_id}]->(n2)",
					provenance.getLeftType(),
					provenance.getRightType(),
					provenance.getRelationType());
			session.run(
					edgeQuery,
					Values.parameters(
							"left_id", provenance.getLeft().toString(),
							"right_id", provenance.getRight().toString(),
							"user_id", provenance.getUserId() != null ? provenance.getUserId() : "",
							"provenance_id", provenance.getId().toString()));
		}

		return provenance;
	}

	public void deleteProvenance(UUID id) {

		final Optional<Provenance> provenance = getProvenance(id);
		if (provenance.isEmpty()) {
			return;
		}

		provenance.get().setDeletedOn(Timestamp.from(Instant.now()));
		provenanceRepository.save(provenance.get());

		try (Session session = neo4jService.getSession()) {
			String query = String.format(
					"MATCH (n1:%s {id: $left_id}) MATCH (n2:%s {id: $right_id}) MATCH (n1)-[r:%s]->(n2) DELETE r",
					provenance.get().getLeftType(),
					provenance.get().getRightType(),
					provenance.get().getRelationType());
			session.run(
					query,
					Values.parameters(
							"left_id", provenance.get().getLeft().toString(),
							"right_id", provenance.get().getRight().toString()));
		}
	}

	public Optional<Provenance> getProvenance(UUID id) {
		return provenanceRepository.getByIdAndDeletedOnIsNull(id);
	}

	public void deleteHangingNodes() {
		try (Session session = neo4jService.getSession()) {
			String query = "MATCH (n) WHERE NOT (n)--() DELETE n";
			session.run(query);
		}
	}
}
