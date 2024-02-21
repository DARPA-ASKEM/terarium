package software.uncharted.terarium.hmiserver.service.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.*;
import software.uncharted.terarium.hmiserver.service.neo4j.Graph;
import software.uncharted.terarium.hmiserver.service.neo4j.Neo4jService;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProvenanceSearchService {

	private final Neo4jService neo4jService;

	// Search methods

	/**
	 *
	 * Return all connected nodes to the provided node.
	 *
	 * @param payload - Search param payload.
	 * @return
	 */
	public ProvenanceSearchResult connectedNodes(final ProvenanceQueryParam payload) {
		try (final Session session = neo4jService.getSession()) {

			final String matchNode = matchNodeBuilder(payload.getRootType(), payload.getRootId());
			final String nodeAbbr = returnNodeAbbr(payload.getRootType());

			final String relationshipsStr = relationshipsArrayAsStr(
					Arrays.asList(ProvenanceRelationType.CONTAINS, ProvenanceRelationType.IS_CONCEPT_OF),
					new ArrayList<>());

			final String limit = payload.getLimit() != null ? "limit: " + payload.getLimit() + ", " : "";
			final String hops = payload.getHops() != null ? "maxLevel: " + payload.getHops() + ", " : "";

			final String query = matchNode + " CALL apoc.path.subgraphAll(" + nodeAbbr + ", {"
					+ "relationshipFilter: '" + relationshipsStr + "',"
					+ "minLevel: 0, "
					+ limit
					+ hops
					+ "whitelistNodes: []"
					+ "}) YIELD nodes, relationships RETURN nodes, relationships";

			final Result result = session.run(query);

			return nodesEdges(result, payload);
		}
	}

	private ProvenanceSearchResult connectedNodesByDirection(final ProvenanceQueryParam payload, final String direction) {
		try (final Session session = neo4jService.getSession()) {
			final String relationshipsStr = relationshipsArrayAsStr(Arrays.asList(ProvenanceRelationType.CONTAINS,
					ProvenanceRelationType.IS_CONCEPT_OF), new ArrayList<>());
			final String relationDirection = dynamicRelationshipDirection(direction,
					String.format("r:'%s' *1..", relationshipsStr));
			final String matchNode = matchNodeBuilder(payload.getRootType(), payload.getRootId());
			final String nodeAbbr = returnNodeAbbr(payload.getRootType());

			final String query = String.format("'%s''%s'(n) return '%s', r, n", matchNode, relationDirection, nodeAbbr);

			final Result response = session.run(query);

			return nodesEdges(response, payload);
		}
	}

	/**
	 *
	 * Return all child nodes.
	 *
	 * @param payload - Search param payload.
	 * @return
	 */
	public ProvenanceSearchResult childNodes(final ProvenanceQueryParam payload) {
		return connectedNodesByDirection(payload, "child");
	}

	/**
	 *
	 * Return all parent nodes.
	 *
	 * @param payload - Search param payload.
	 * @return
	 */
	public ProvenanceSearchResult parentNodes(final ProvenanceQueryParam payload) {
		return connectedNodesByDirection(payload, "parent");
	}

	/**
	 *
	 * Identifies which model revisions help create the latest model which was used
	 * to create the artifact.
	 *
	 * @param payload - Search param payload.
	 * @return
	 */
	public ProvenanceSearchResult parentModelRevisions(final ProvenanceQueryParam payload) {
		if (!Arrays
				.asList(ProvenanceType.MODEL, ProvenanceType.SIMULATION_RUN, ProvenanceType.PLAN,
						ProvenanceType.DATASET)
				.contains(payload.getRootType())) {
			throw new IllegalArgumentException(
					"Derived models can only be found from root types of Model, SimulationRun, Plan and Dataset");
		}

		try (final Session session = neo4jService.getSession()) {
			final String matchPattern = parentModelQueryGenerator(payload.getRootType(), payload.getRootId());
			final String relationshipsStr = relationshipsArrayAsStr(
					Arrays.asList(ProvenanceRelationType.CONTAINS, ProvenanceRelationType.IS_CONCEPT_OF),
					new ArrayList<>());

			final String query = String.format(
					"'%s' OPTIONAL MATCH (Mr2:ModelRevision)-[r2:'%s' *1..]->(Mr) WITH *, COLLECT(r)+COLLECT(r2) AS r3, COLLECT(Mr)+COLLECT(Mr2) AS Mrs UNWIND Mrs AS Both_rms UNWIND r3 AS r4 WITH * OPTIONAL MATCH (Both_rms)<-[r5:BEGINS_AT]-(Md:Model) WITH *, COLLECT(r4)+COLLECT(r5) AS r6 UNWIND r6 AS r7 RETURN Both_rms, Md, r7",
					matchPattern, relationshipsStr);

			final Result response = session.run(query);

			return nodesEdges(response, payload);
		}
	}

	/**
	 *
	 * Identifies which models help create the latest model
	 *
	 * @param payload - Search param payload.
	 * @return
	 */
	public ProvenanceSearchResult parentModels(final ProvenanceQueryParam payload) {
		if (!Arrays.asList(ProvenanceType.MODEL, ProvenanceType.SIMULATION_RUN, ProvenanceType.PLAN,
				ProvenanceType.DATASET).contains(payload.getRootType())) {
			throw new IllegalArgumentException(
					"Parent models can only be found from root types of Model, Plan, SimulationRun, Dataset");
		}

		try (final Session session = neo4jService.getSession()) {
			final String matchPattern = parentModelQueryGenerator(payload.getRootType(), payload.getRootId());
			final String nodeAbbr = returnNodeAbbr(payload.getRootType());

			final String modelRelationships = relationshipsArrayAsStr(
					new ArrayList<>(),
					Arrays.asList(
							ProvenanceRelationType.EDITED_FROM,
							ProvenanceRelationType.COPIED_FROM,
							ProvenanceRelationType.GLUED_FROM,
							ProvenanceRelationType.DECOMPOSED_FROM,
							ProvenanceRelationType.STRATIFIED_FROM));

			final String query = String.format(
					"'%s' OPTIONAL MATCH (Mr)-[r2:'%s' *1..]->(Mr2:ModelRevision) WITH *, COLLECT(Mr)+COLLECT(Mr2) AS Mrs, COLLECT(r)+COLLECT(r2) AS r3 UNWIND Mrs AS Both_rms WITH * OPTIONAL MATCH (md2:Model)-[r4:BEGINS_AT]->(Both_rms) WITH *, COLLECT(r3)+COLLECT(r4) AS r5 RETURN '%s', md2, Both_rms, r5",
					matchPattern, modelRelationships, nodeAbbr);

			final Result response = session.run(query);

			return nodesEdges(response, payload);
		}
	}

	/**
	 *
	 * Identifies which nodes were created by user.
	 *
	 * @param payload - Search param payload.
	 * @return
	 */
	public ProvenanceSearchResult artifactsCreatedByUser(final ProvenanceQueryParam payload) {
		try (final Session session = neo4jService.getSession()) {
			final String matchNode = matchNodeBuilder();

			final String query = String.format(
					"'%s'-[r]->(n2) WHERE r.user_id='%s' WITH *, COLLECT(n)+COLLECT(n2) AS nodes UNWIND nodes AS both_nodes WITH * RETURN both_nodes",
					matchNode, payload.getUserId());

			final Result response = session.run(query);

			return nodesEdges(response, payload);
		}
	}

	/**
	 *
	 * Identifies which nodes are associated with a concept
	 *
	 * @param payload - Search param payload.
	 * @return
	 */
	public ProvenanceSearchResult concept(final ProvenanceQueryParam payload) {
		try (final Session session = neo4jService.getSession()) {
			final String matchNode = matchNodeBuilder(ProvenanceType.CONCEPT);

			final String query = String.format(
					"'%s'-[r:IS_CONCEPT_OF]->(n) WHERE Cn.concept='%s' RETURN n",
					matchNode, payload.getCurie());

			final Result response = session.run(query);

			return nodesEdges(response, payload);
		}
	}

	/**
	 *
	 * Identifies the code from which a model was extracted
	 *
	 * @param payload - Search param payload.
	 * @return
	 */
	public Set<String> modelsFromCode(final ProvenanceQueryParam payload) {
		if (payload.getRootType() != ProvenanceType.MODEL) {
			throw new IllegalArgumentException("Code used for model extraction can only be found by providing a Model");
		}

		try (final Session session = neo4jService.getSession()) {
			final UUID modelId = payload.getRootId();

			final String query = String.format("MATCH (c:Code)<-[r:EXTRACTED_FROM]-(m:Model {id: '%s'}) RETURN c", modelId);

			final Result response = session.run(query);

			final Set<String> responseData = new HashSet<>();
			while (response.hasNext()) {
				responseData.add(response.next().get("c").get("id").asString());
			}

			return responseData;
		}
	}

	/**
	 *
	 * Identifies the equation from which a model was extracted
	 *
	 * @param payload - Search param payload.
	 * @return
	 */
	public Set<String> modelsFromEquation(final ProvenanceQueryParam payload) {
		if (payload.getRootType() != ProvenanceType.MODEL) {
			throw new IllegalArgumentException(
					"Equation used for model extraction can only be found by providing a Model");
		}

		try (final Session session = neo4jService.getSession()) {
			final UUID modelId = payload.getRootId();

			final String query = String.format("MATCH (e:Equation)<-[r:EXTRACTED_FROM]-(m:Model {id: '%s'}) RETURN e",
					modelId);

			final Result response = session.run(query);

			final Set<String> responseData = new HashSet<>();
			while (response.hasNext()) {
				responseData.add(response.next().get("e").get("id").asString());
			}

			return responseData;
		}
	}

	/**
	 *
	 * Identifies the document from which a model configuration was extracted
	 *
	 * @param payload - Search param payload.
	 * @return
	 */
	public Set<String> modelConfigFromDocument(final ProvenanceQueryParam payload) {
		if (payload.getRootType() != ProvenanceType.MODEL_CONFIGURATION) {
			throw new IllegalArgumentException(
					"Document used for model configuration extraction can only be found by providing a Model confirguration");
		}

		try (final Session session = neo4jService.getSession()) {
			final UUID modelId = payload.getRootId();

			final String query = String.format("MATCH (d:Document)<-[r:EXTRACTED_FROM]-(m:ModelConfiguration {id: '%s'}) RETURN d",
					modelId);

			final Result response = session.run(query);

			log.info("Response: " + response.toString());
			final Set<String> responseData = new HashSet<>();
			while (response.hasNext()) {
				responseData.add(response.next().get("d").get("id").asString());
			}

			log.info("Response Data: "  + responseData.toString());
			return responseData;
		}
	}

	/**
	 *
	 * Identifies the document from which a model was extracted
	 *
	 * @param payload - Search param payload.
	 * @return
	 */
	public Set<String> modelsFromDocument(final ProvenanceQueryParam payload) {
		if (payload.getRootType() != ProvenanceType.MODEL) {
			throw new IllegalArgumentException(
					"Document used for model extraction can only be found by providing a Model");
		}

		try (final Session session = neo4jService.getSession()) {
			final UUID modelId = payload.getRootId();

			final String query = String.format("MATCH (d:Document)<-[r:EXTRACTED_FROM]-(m:Model {id: '%s'}) RETURN d",
					modelId);

			final Result response = session.run(query);

			final Set<String> responseData = new HashSet<>();
			while (response.hasNext()) {
				responseData.add(response.next().get("d").get("id").asString());
			}

			return responseData;
		}
	}

	/**
	 *
	 * Counts of which nodes are associated with a concept
	 *
	 * @param payload - Search param payload.
	 * @return
	 */
	public Map<String, Integer> conceptCounts(final ProvenanceQueryParam payload) {
		try (final Session session = neo4jService.getSession()) {
			final String matchNode = matchNodeBuilder(ProvenanceType.CONCEPT);

			final String query = String.format(
					"'%s'-[r:IS_CONCEPT_OF]->(n) WHERE Cn.concept='%s' RETURN labels(n) as label, n.id as id",
					matchNode, payload.getCurie());

			final Result response = session.run(query);

			final Map<String, Integer> counts = new HashMap<>();
			while (response.hasNext()) {
				final Record r = response.next();
				final String label = r.get("label").asList().get(0).toString();
				counts.put(label, counts.getOrDefault(label, 0) + 1);
			}

			return counts;
		}
	}

	/**
	 *
	 * Return models extracted from a document, code, or equation.
	 *
	 * @param payload - Search param payload.
	 * @return
	 */
	public Set<String> extractedModels(final ProvenanceQueryParam payload) {
		final ProvenanceType rootType = payload.getRootType();
		if (rootType != ProvenanceType.DOCUMENT && rootType != ProvenanceType.CODE
				&& rootType != ProvenanceType.EQUATION) {
			throw new IllegalArgumentException(
					"Derived models can only be found from root types of Document, Code, or Equation");
		}

		try (final Session session = neo4jService.getSession()) {
			final String generatedQuery = extractedModelsQueryGenerator(rootType, payload.getRootId());

			final Result response = session.run(generatedQuery);

			final Set<String> responseData = new HashSet<>();
			while (response.hasNext()) {
				responseData.add(response.next().get("m").get("id").asString());
			}

			return responseData;
		}
	}

	public Map<String, Object> modelDocument(final ProvenanceQueryParam payload) {
		try (final Session session = neo4jService.getSession()) {
			final String query = String.format(
					"MATCH (Md:Model {id:'%s'})<-[r:REINTERPRETS|EXTRACTED_FROM|BEGINS_AT *1..]->(Do:Document) RETURN Do",
					payload.getRootId());

			final Result response = session.run(query);
			if (!response.hasNext()) {
				return null;
			}

			return response.next().get("Do").asMap();
		}
	}

	// Util methods

	public ProvenanceSearchResult nodesEdges(final Result response, final ProvenanceQueryParam payload) {

		final boolean includeEdges = payload.getEdges() != null ? payload.getEdges() : false;
		final boolean includeNodes = payload.getNodes() != null ? payload.getNodes() : true;
		final boolean includeVersions = payload.getVersions() != null ? payload.getVersions() : false;
		final List<ProvenanceType> types = payload.getTypes() != null ? payload.getTypes() : new ArrayList<>();

		final Graph graph = new Graph(response);

		final ProvenanceSearchResult result = new ProvenanceSearchResult();

		if (includeEdges) {
			final List<ProvenanceEdge> edges = formattedEdges(graph);

			if (!includeVersions) {
				for (final ProvenanceEdge edge : edges) {
					if (edge.getRelationType() == ProvenanceRelationType.BEGINS_AT) {

						final ProvenanceNode model = edge.getLeft();
						final ProvenanceNode modelRevision = edge.getRight();
						modelRevisionsToModel(model, modelRevision, edges);
					}
				}
				result.setEdges(filterRelationshipTypes(edges, types));
			}
		} else {
			result.setEdges(new ArrayList<>());
		}

		if (includeNodes) {
			result.setNodes(filterNodeTypes(formattedNodes(graph), types));
		} else {
			result.setEdges(new ArrayList<>());
		}

		return result;
	}

	public static void modelRevisionsToModel(final ProvenanceNode model, final ProvenanceNode modelRevision,
																					 final List<ProvenanceEdge> edges) {

		for (final ProvenanceEdge edge : edges) {
			if (edge.getRight().equals(modelRevision)) {
				edge.setRight(model);
				final ProvenanceNode left = edge.getLeft();
				if (left.getType() == ProvenanceType.MODEL_REVISION
						&& ProvenanceRelationType.EDITED_FROM == edge.getRelationType()) {
					modelRevisionsToModel(model, left, edges);
				}
			}

			if (edge.getLeft().equals(modelRevision)) {
				edge.setLeft(model);
				final ProvenanceNode right = edge.getRight();
				if (right.getType() == ProvenanceType.MODEL_REVISION
						&& ProvenanceRelationType.EDITED_FROM == edge.getRelationType()) {
					modelRevisionsToModel(model, right, edges);
				}
			}
		}
	}

	public static List<ProvenanceEdge> filterRelationshipTypes(final List<ProvenanceEdge> relationships,
																														 final List<ProvenanceType> includedTypes) {
		if (includedTypes.size() == 0) {
			return relationships;
		}
		final List<ProvenanceEdge> clipped = new ArrayList<>();
		for (final ProvenanceEdge relation : relationships) {
			final ProvenanceNode left = relation.getLeft();
			final ProvenanceNode right = relation.getRight();
			if (!left.equals(right)
					&& includedTypes.contains(left.getType())
					&& includedTypes.contains(right.getType())) {
				clipped.add(relation);
			}
		}
		return clipped;
	}

	public static List<ProvenanceNode> filterNodeTypes(final List<ProvenanceNode> nodes, final List<ProvenanceType> includedTypes) {
		if (includedTypes.size() == 0) {
			return nodes;
		}
		final List<ProvenanceNode> res = new ArrayList<>();
		for (final ProvenanceNode node : nodes) {
			if (includedTypes.contains(node.getType())) {
				res.add(node);
			}
		}
		return res;
	}

	public static List<ProvenanceNode> formattedNodes(final Graph graph) {
		final List<ProvenanceNode> nodes = new ArrayList<>();
		for (final Node node : graph.getNodes()) {
			try {
				final String label = node.labels().iterator().next();

				final ProvenanceNode formatted = new ProvenanceNode();
				formatted.setId(UUID.fromString(node.get("id").asString()));
				formatted.setType(ProvenanceType.findByType(label));

				nodes.add(formatted);

			} catch (final NoSuchElementException e) {
				log.warn("No element found: " + e);
				continue;
			}
		}
		return nodes;
	}

	public static String dynamicRelationshipDirection(final String direction, final String relationshipType) {
		switch (direction) {
			case "all":
				return String.format("-['%s']-", relationshipType);
			case "child":
				return String.format("<-['%s']-", relationshipType);
			case "parent":
				return String.format("-['%s']->", relationshipType);
			default:
				throw new IllegalArgumentException("Relationship direction is not allowed.");
		}
	}

	public static String extractedModelsQueryGenerator(final ProvenanceType rootType, final UUID rootId) {
		switch (rootType) {
			case DOCUMENT:
				return String.format("MATCH (m:Model)-[:EXTRACTED_FROM]->(d:Document {id:'%s'}) RETURN m", rootId);
			case CODE:
				return String.format("MATCH (m:Model)-[:EXTRACTED_FROM]->(c:Code {id:'%s'}) RETURN m", rootId);
			case EQUATION:
				return String.format("MATCH (m:Model)-[:EXTRACTED_FROM]->(e:Equation {id:'%s'}) RETURN m", rootId);
			default:
				throw new IllegalArgumentException("Models cannot be derived from this type: " + rootType);
		}
	}

	public String parentModelQueryGenerator(final ProvenanceType rootType, final UUID rootId) {
		final String matchNode = matchNodeBuilder(rootType, rootId);
		final String relationshipsStr = relationshipsArrayAsStr(
				Arrays.asList(ProvenanceRelationType.CONTAINS, ProvenanceRelationType.IS_CONCEPT_OF),
				new ArrayList<>());
		final String modelRevisionNode = nodeBuilder(ProvenanceType.MODEL);

		final Map<ProvenanceType, String> queryTemplatesIndex = new HashMap<>();
		queryTemplatesIndex.put(ProvenanceType.MODEL, String.format("-[r:BEGINS_AT]->'%s' ", modelRevisionNode));
		queryTemplatesIndex.put(ProvenanceType.MODEL_CONFIGURATION,
				String.format("-[r:USES]->'%s' ", modelRevisionNode));
		queryTemplatesIndex.put(ProvenanceType.SIMULATION,
				String.format("-[r:'%s' *1..]->'%s' ", relationshipsStr, modelRevisionNode));
		queryTemplatesIndex.put(ProvenanceType.DATASET,
				String.format("-[r:'%s' *1..]->'%s' ", relationshipsStr, modelRevisionNode));

		return matchNode + queryTemplatesIndex.get(rootType);
	}

	public static String matchNodeBuilder() {
		return "MATCH (n) ";
	}

	public String matchNodeBuilder(final ProvenanceType nodeType) {
		final String nodeTypeCharacter = returnNodeAbbr(nodeType);
		return String.format("MATCH (%s:%s)", nodeTypeCharacter, nodeType);
	}

	public String matchNodeBuilder(final ProvenanceType nodeType, final UUID nodeId) {
		if (nodeType == null) {
			return "MATCH (n) ";
		}
		final String nodeTypeCharacter = returnNodeAbbr(nodeType);
		if (nodeId == null) {
			return String.format("MATCH (%s:%s)", nodeTypeCharacter, nodeType);
		}
		return String.format("MATCH (%s:%s {id: '%s'}) ", nodeTypeCharacter, nodeType, nodeId);
	}

	public String returnNodeAbbr(final ProvenanceType nodeType) {
		final Map<ProvenanceType, String> provenanceTypeToAbbr = new HashMap<>() {
            {
                put(ProvenanceType.DATASET, "Ds");
                put(ProvenanceType.MODEL, "Md");
                put(ProvenanceType.MODEL_CONFIGURATION, "Mc");
                put(ProvenanceType.DOCUMENT, "Do");
                put(ProvenanceType.SIMULATION, "Si");
                put(ProvenanceType.PROJECT, "Pr");
                put(ProvenanceType.CONCEPT, "Cn");
                put(ProvenanceType.ARTIFACT, "Ar");
                put(ProvenanceType.CODE, "Co");
                put(ProvenanceType.EQUATION, "Eq");
            }
        };

		return provenanceTypeToAbbr.get(nodeType);
	}

	public static String relationshipsArrayAsStr(final List<ProvenanceRelationType> exclude, final List<ProvenanceRelationType> include) {
		final StringBuilder relationshipStr = new StringBuilder();
		if (exclude != null) {
			for (final ProvenanceRelationType type : ProvenanceRelationType.values()) {
				if (exclude.contains(type)) {
					continue;
				}
				final String value = type.name();
				relationshipStr.append(value).append("|");
			}
			return relationshipStr.substring(0, relationshipStr.length() - 1);
		}
		for (final ProvenanceRelationType type : ProvenanceRelationType.values()) {
			if (include.contains(type)) {
				final String value = type.name();
				relationshipStr.append(value).append("|");
			}
		}
		return relationshipStr.substring(0, relationshipStr.length() - 1);
	}

	public String nodeBuilder(final ProvenanceType nodeType, final String nodeId) {
		if (nodeType == null) {
			return "(n) ";
		}
		final String nodeTypeAbbr = returnNodeAbbr(nodeType);
		return String.format("('%s':'%s' {id: '%s'}) ", nodeTypeAbbr, nodeType, nodeId);
	}

	public String nodeBuilder(final ProvenanceType nodeType) {
		if (nodeType == null) {
			return "(n) ";
		}
		final String nodeTypeAbbr = returnNodeAbbr(nodeType);
		return String.format("('%s':'%s')", nodeTypeAbbr, nodeType);
	}

	public List<ProvenanceEdge> formattedEdges(final Graph graph) {
		final List<ProvenanceEdge> edges = new ArrayList<>();
		for (final Relationship relationship : graph.getRelationships()) {
			try {
				final String startId = relationship.startNodeElementId();
				final String endId = relationship.endNodeElementId();
				final Node startNode = graph.getNodesById().get(startId);
				final Node endNode = graph.getNodesById().get(endId);

				final String startLabel = startNode.labels().iterator().next();
				final String endLabel = endNode.labels().iterator().next();

				final ProvenanceNode left = new ProvenanceNode();
				left.setId(UUID.fromString(startNode.get("id").asString()));
				left.setType(ProvenanceType.findByType(startLabel));

				final ProvenanceNode right = new ProvenanceNode();
				right.setId(UUID.fromString(endNode.get("id").asString()));
				right.setType(ProvenanceType.findByType(endLabel));

				final ProvenanceEdge edge = new ProvenanceEdge();
				edge.setRelationType(ProvenanceRelationType.findByType(relationship.type()));
				edge.setId(UUID.fromString(relationship.get("provenance_id").asString()));
				edge.setLeft(left);
				edge.setRight(right);

				edges.add(edge);
			} catch (final NoSuchElementException e) {
				log.warn("No element found: " + e);
				continue;
			}
		}
		return edges;
	}

}
