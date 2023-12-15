package software.uncharted.terarium.hmiserver.service.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceEdge;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceNode;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceQueryParam;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceRelationType;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceSearchResult;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceType;
import software.uncharted.terarium.hmiserver.service.neo4j.Graph;
import software.uncharted.terarium.hmiserver.service.neo4j.Neo4jService;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProvenanceSearchService {

	private final Neo4jService neo4jService;

	// Search methods

	public ProvenanceSearchResult connectedNodes(ProvenanceQueryParam payload) {
		try (Session session = neo4jService.getSession()) {

			String matchNode = matchNodeBuilder(payload.getRootType(), payload.getRootId());
			String nodeAbbr = returnNodeAbbr(payload.getRootType());

			String relationshipsStr = relationshipsArrayAsStr(
					Arrays.asList(ProvenanceRelationType.CONTAINS, ProvenanceRelationType.IS_CONCEPT_OF),
					new ArrayList<>());

			String limit = payload.getLimit() != null ? "limit: " + payload.getLimit() + ", " : "";
			String hops = payload.getHops() != null ? "maxLevel: " + payload.getHops() + ", " : "";

			String query = matchNode + " CALL apoc.path.subgraphAll(" + nodeAbbr + ", {"
					+ "relationshipFilter: '" + relationshipsStr + "',"
					+ "minLevel: 0, "
					+ limit
					+ hops
					+ "whitelistNodes: []"
					+ "}) YIELD nodes, relationships RETURN nodes, relationships";

			Result result = session.run(query);

			return nodesEdges(result, payload);
		}
	}

	private ProvenanceSearchResult connectedNodesByDirection(ProvenanceQueryParam payload, String direction) {
		try (Session session = neo4jService.getSession()) {
			String relationshipsStr = relationshipsArrayAsStr(Arrays.asList(ProvenanceRelationType.CONTAINS,
					ProvenanceRelationType.IS_CONCEPT_OF), new ArrayList<>());
			String relationDirection = dynamicRelationshipDirection(direction,
					String.format("r:'%s' *1..", relationshipsStr));
			String matchNode = matchNodeBuilder(payload.getRootType(), payload.getRootId());
			String nodeAbbr = returnNodeAbbr(payload.getRootType());

			String query = String.format("'%s''%s'(n) return '%s', r, n", matchNode, relationDirection, nodeAbbr);

			Result response = session.run(query);

			return nodesEdges(response, payload);
		}
	}

	public ProvenanceSearchResult childNodes(ProvenanceQueryParam payload) {
		return connectedNodesByDirection(payload, "child");
	}

	public ProvenanceSearchResult parentNodes(ProvenanceQueryParam payload) {
		return connectedNodesByDirection(payload, "parent");
	}

	public ProvenanceSearchResult parentModelRevisions(ProvenanceQueryParam payload) {
		if (!Arrays
				.asList(ProvenanceType.MODEL, ProvenanceType.SIMULATION_RUN, ProvenanceType.PLAN,
						ProvenanceType.DATASET)
				.contains(payload.getRootType())) {
			throw new IllegalArgumentException(
					"Derived models can only be found from root types of Model, SimulationRun, Plan and Dataset");
		}

		try (Session session = neo4jService.getSession()) {
			String matchPattern = parentModelQueryGenerator(payload.getRootType(), payload.getRootId());
			String relationshipsStr = relationshipsArrayAsStr(
					Arrays.asList(ProvenanceRelationType.CONTAINS, ProvenanceRelationType.IS_CONCEPT_OF),
					new ArrayList<>());

			String query = String.format(
					"'%s' OPTIONAL MATCH (Mr2:ModelRevision)-[r2:'%s' *1..]->(Mr) WITH *, COLLECT(r)+COLLECT(r2) AS r3, COLLECT(Mr)+COLLECT(Mr2) AS Mrs UNWIND Mrs AS Both_rms UNWIND r3 AS r4 WITH * OPTIONAL MATCH (Both_rms)<-[r5:BEGINS_AT]-(Md:Model) WITH *, COLLECT(r4)+COLLECT(r5) AS r6 UNWIND r6 AS r7 RETURN Both_rms, Md, r7",
					matchPattern, relationshipsStr);

			Result response = session.run(query);

			return nodesEdges(response, payload);
		}
	}

	public ProvenanceSearchResult parentModels(ProvenanceQueryParam payload) {
		if (!Arrays.asList(ProvenanceType.MODEL, ProvenanceType.SIMULATION_RUN, ProvenanceType.PLAN,
				ProvenanceType.DATASET).contains(payload.getRootType())) {
			throw new IllegalArgumentException(
					"Parent models can only be found from root types of Model, Plan, SimulationRun, Dataset");
		}

		try (Session session = neo4jService.getSession()) {
			String matchPattern = parentModelQueryGenerator(payload.getRootType(), payload.getRootId());
			String nodeAbbr = returnNodeAbbr(payload.getRootType());

			String modelRelationships = relationshipsArrayAsStr(
					new ArrayList<>(),
					Arrays.asList(
							ProvenanceRelationType.EDITED_FROM,
							ProvenanceRelationType.COPIED_FROM,
							ProvenanceRelationType.GLUED_FROM,
							ProvenanceRelationType.DECOMPOSED_FROM,
							ProvenanceRelationType.STRATIFIED_FROM));

			String query = String.format(
					"'%s' OPTIONAL MATCH (Mr)-[r2:'%s' *1..]->(Mr2:ModelRevision) WITH *, COLLECT(Mr)+COLLECT(Mr2) AS Mrs, COLLECT(r)+COLLECT(r2) AS r3 UNWIND Mrs AS Both_rms WITH * OPTIONAL MATCH (md2:Model)-[r4:BEGINS_AT]->(Both_rms) WITH *, COLLECT(r3)+COLLECT(r4) AS r5 RETURN '%s', md2, Both_rms, r5",
					matchPattern, modelRelationships, nodeAbbr);

			Result response = session.run(query);

			return nodesEdges(response, payload);
		}
	}

	public ProvenanceSearchResult artifactsCreatedByUser(ProvenanceQueryParam payload) {
		try (Session session = neo4jService.getSession()) {
			String matchNode = matchNodeBuilder();

			String query = String.format(
					"'%s'-[r]->(n2) WHERE r.user_id='%s' WITH *, COLLECT(n)+COLLECT(n2) AS nodes UNWIND nodes AS both_nodes WITH * RETURN both_nodes",
					matchNode, payload.getUserId());

			Result response = session.run(query);

			return nodesEdges(response, payload);
		}
	}

	public ProvenanceSearchResult concept(ProvenanceQueryParam payload) {
		try (Session session = neo4jService.getSession()) {
			String matchNode = matchNodeBuilder(ProvenanceType.CONCEPT);

			String query = String.format(
					"'%s'-[r:IS_CONCEPT_OF]->(n) WHERE Cn.concept='%s' RETURN n",
					matchNode, payload.getCurie());

			Result response = session.run(query);

			return nodesEdges(response, payload);
		}
	}

	public Set<String> modelsFromCode(ProvenanceQueryParam payload) {
		if (payload.getRootType() != ProvenanceType.MODEL) {
			throw new IllegalArgumentException("Code used for model extraction can only be found by providing a Model");
		}

		try (Session session = neo4jService.getSession()) {
			UUID modelId = payload.getRootId();

			String query = String.format("MATCH (c:Code)<-[r:EXTRACTED_FROM]-(m:Model {id: '%s'}) RETURN c", modelId);

			Result response = session.run(query);

			Set<String> responseData = new HashSet<>();
			while (response.hasNext()) {
				responseData.add(response.next().get("c").get("id").asString());
			}

			return responseData;
		}
	}

	public Set<String> modelsFromEquation(ProvenanceQueryParam payload) {
		if (payload.getRootType() != ProvenanceType.MODEL) {
			throw new IllegalArgumentException(
					"Equation used for model extraction can only be found by providing a Model");
		}

		try (Session session = neo4jService.getSession()) {
			UUID modelId = payload.getRootId();

			String query = String.format("MATCH (e:Equation)<-[r:EXTRACTED_FROM]-(m:Model {id: '%s'}) RETURN e",
					modelId);

			Result response = session.run(query);

			Set<String> responseData = new HashSet<>();
			while (response.hasNext()) {
				responseData.add(response.next().get("e").get("id").asString());
			}

			return responseData;
		}
	}

	public Set<String> modelsFromDocument(ProvenanceQueryParam payload) {
		if (payload.getRootType() != ProvenanceType.MODEL) {
			throw new IllegalArgumentException(
					"Document used for model extraction can only be found by providing a Model");
		}

		try (Session session = neo4jService.getSession()) {
			UUID modelId = payload.getRootId();

			String query = String.format("MATCH (d:Document)<-[r:EXTRACTED_FROM]-(m:Model {id: '%s'}) RETURN d",
					modelId);

			Result response = session.run(query);

			Set<String> responseData = new HashSet<>();
			while (response.hasNext()) {
				responseData.add(response.next().get("d").get("id").asString());
			}

			return responseData;
		}
	}

	public Map<String, Integer> conceptCounts(ProvenanceQueryParam payload) {
		try (Session session = neo4jService.getSession()) {
			String matchNode = matchNodeBuilder(ProvenanceType.CONCEPT);

			String query = String.format(
					"'%s'-[r:IS_CONCEPT_OF]->(n) WHERE Cn.concept='%s' RETURN labels(n) as label, n.id as id",
					matchNode, payload.getCurie());

			Result response = session.run(query);

			Map<String, Integer> counts = new HashMap<>();
			while (response.hasNext()) {
				Record record = response.next();
				String label = record.get("label").asList().get(0).toString();
				counts.put(label, counts.getOrDefault(label, 0) + 1);
			}

			return counts;
		}
	}

	public Set<String> extractedModels(ProvenanceQueryParam payload) {
		ProvenanceType rootType = payload.getRootType();
		if (rootType != ProvenanceType.DOCUMENT && rootType != ProvenanceType.CODE
				&& rootType != ProvenanceType.EQUATION) {
			throw new IllegalArgumentException(
					"Derived models can only be found from root types of Document, Code, or Equation");
		}

		try (Session session = neo4jService.getSession()) {
			String generatedQuery = extractedModelsQueryGenerator(rootType, payload.getRootId());

			Result response = session.run(generatedQuery);

			Set<String> responseData = new HashSet<>();
			while (response.hasNext()) {
				responseData.add(response.next().get("m").get("id").asString());
			}

			return responseData;
		}
	}

	public Map<String, Object> modelDocument(ProvenanceQueryParam payload) {
		try (Session session = neo4jService.getSession()) {
			String query = String.format(
					"MATCH (Md:Model {id:'%s'})<-[r:REINTERPRETS|EXTRACTED_FROM|BEGINS_AT *1..]->(Do:Document) RETURN Do",
					payload.getRootId());

			Result response = session.run(query);
			if (!response.hasNext()) {
				return null;
			}

			return response.next().get("Do").asMap();
		}
	}

	// Util methods

	public ProvenanceSearchResult nodesEdges(Result response, ProvenanceQueryParam payload) {

		boolean includeEdges = payload.getEdges() != null ? payload.getEdges() : false;
		boolean includeNodes = payload.getNodes() != null ? payload.getNodes() : true;
		boolean includeVersions = payload.getVersions() != null ? payload.getVersions() : false;
		List<ProvenanceType> types = payload.getTypes() != null ? payload.getTypes() : new ArrayList<>();

		Graph graph = new Graph(response);

		ProvenanceSearchResult result = new ProvenanceSearchResult();

		if (includeEdges) {
			List<ProvenanceEdge> edges = formattedEdges(graph);

			if (!includeVersions) {
				for (ProvenanceEdge edge : edges) {
					if (edge.getRelationType() == ProvenanceRelationType.BEGINS_AT) {

						ProvenanceNode model = edge.getLeft();
						ProvenanceNode modelRevision = edge.getRight();
						modelRevisionsToModel(model, modelRevision, edges);
					}
				}
				result.setEdges(filterRelationshipTypes(edges, types));
			}
		}

		if (includeNodes) {
			result.setNodes(filterNodeTypes(formattedNodes(graph), types));
		}

		return result;
	}

	public void modelRevisionsToModel(ProvenanceNode model, ProvenanceNode modelRevision,
			final List<ProvenanceEdge> edges) {

		for (ProvenanceEdge edge : edges) {
			if (edge.getRight().equals(modelRevision)) {
				edge.setRight(model);
				ProvenanceNode left = edge.getLeft();
				if (left.getType() == ProvenanceType.MODEL_REVISION
						&& ProvenanceRelationType.EDITED_FROM == edge.getRelationType()) {
					modelRevisionsToModel(model, left, edges);
				}
			}

			if (edge.getLeft().equals(modelRevision)) {
				edge.setLeft(model);
				ProvenanceNode right = edge.getRight();
				if (right.getType() == ProvenanceType.MODEL_REVISION
						&& ProvenanceRelationType.EDITED_FROM == edge.getRelationType()) {
					modelRevisionsToModel(model, right, edges);
				}
			}
		}
	}

	public List<ProvenanceEdge> filterRelationshipTypes(List<ProvenanceEdge> relationships,
			List<ProvenanceType> includedTypes) {
		List<ProvenanceEdge> clipped = new ArrayList<>();
		for (ProvenanceEdge relation : relationships) {
			ProvenanceNode left = relation.getLeft();
			ProvenanceNode right = relation.getRight();
			if (!left.equals(right)
					&& includedTypes.contains(left.getType())
					&& includedTypes.contains(right.getType())) {
				clipped.add(relation);
			}
		}
		return clipped;
	}

	public List<ProvenanceNode> filterNodeTypes(List<ProvenanceNode> nodes, List<ProvenanceType> includedTypes) {
		List<ProvenanceNode> res = new ArrayList<>();
		for (ProvenanceNode node : nodes) {
			if (includedTypes.contains(node.getType())) {
				res.add(node);
			}
		}
		return res;
	}

	public List<ProvenanceNode> formattedNodes(Graph graph) {
		List<ProvenanceNode> nodes = new ArrayList<>();
		for (Node node : graph.getNodes()) {
			try {
				String label = node.labels().iterator().next();

				ProvenanceNode formatted = new ProvenanceNode();
				formatted.setId(UUID.fromString(node.elementId()));
				formatted.setType(ProvenanceType.findByType(label));

				nodes.add(formatted);

			} catch (NoSuchElementException e) {
				log.warn("No element found: " + e);
				continue;
			}
		}
		return nodes;
	}

	public String dynamicRelationshipDirection(String direction, String relationshipType) {
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

	public String extractedModelsQueryGenerator(ProvenanceType rootType, UUID rootId) {
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

	public String parentModelQueryGenerator(ProvenanceType rootType, UUID rootId) {
		String matchNode = matchNodeBuilder(rootType, rootId);
		String relationshipsStr = relationshipsArrayAsStr(
				Arrays.asList(ProvenanceRelationType.CONTAINS, ProvenanceRelationType.IS_CONCEPT_OF),
				new ArrayList<>());
		String modelRevisionNode = nodeBuilder(ProvenanceType.MODEL);

		Map<ProvenanceType, String> queryTemplatesIndex = new HashMap<>();
		queryTemplatesIndex.put(ProvenanceType.MODEL, String.format("-[r:BEGINS_AT]->'%s' ", modelRevisionNode));
		queryTemplatesIndex.put(ProvenanceType.MODEL_CONFIGURATION,
				String.format("-[r:USES]->'%s' ", modelRevisionNode));
		queryTemplatesIndex.put(ProvenanceType.SIMULATION,
				String.format("-[r:'%s' *1..]->'%s' ", relationshipsStr, modelRevisionNode));
		queryTemplatesIndex.put(ProvenanceType.DATASET,
				String.format("-[r:'%s' *1..]->'%s' ", relationshipsStr, modelRevisionNode));

		return matchNode + queryTemplatesIndex.get(rootType);
	}

	public String matchNodeBuilder() {
		return "MATCH (n) ";
	}

	public String matchNodeBuilder(ProvenanceType nodeType) {
		String nodeTypeCharacter = returnNodeAbbr(nodeType);
		return String.format("MATCH ('%s':'%s')", nodeTypeCharacter, nodeType);
	}

	public String matchNodeBuilder(ProvenanceType nodeType, UUID nodeId) {
		if (nodeType == null) {
			return "MATCH (n) ";
		}
		String nodeTypeCharacter = returnNodeAbbr(nodeType);
		if (nodeId == null) {
			return String.format("MATCH ('%s':'%s')", nodeTypeCharacter, nodeType);
		}
		return String.format("MATCH ('%s':'%s' {id: '%s'}) ", nodeTypeCharacter, nodeType, nodeId);
	}

	public String returnNodeAbbr(ProvenanceType nodeType) {
		Map<ProvenanceType, String> provenanceTypeToAbbr = new HashMap<ProvenanceType, String>() {
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

	public String relationshipsArrayAsStr(List<ProvenanceRelationType> exclude, List<ProvenanceRelationType> include) {
		StringBuilder relationshipStr = new StringBuilder();
		if (exclude != null) {
			for (ProvenanceRelationType type : ProvenanceRelationType.values()) {
				if (exclude.contains(type)) {
					continue;
				}
				String value = type.name();
				relationshipStr.append(value).append("|");
			}
			return relationshipStr.substring(0, relationshipStr.length() - 1);
		}
		for (ProvenanceRelationType type : ProvenanceRelationType.values()) {
			if (include.contains(type)) {
				String value = type.name();
				relationshipStr.append(value).append("|");
			}
		}
		return relationshipStr.substring(0, relationshipStr.length() - 1);
	}

	public String nodeBuilder(ProvenanceType nodeType, String nodeId) {
		if (nodeType == null) {
			return "(n) ";
		}
		String nodeTypeAbbr = returnNodeAbbr(nodeType);
		return String.format("('%s':'%s' {id: '%s'}) ", nodeTypeAbbr, nodeType, nodeId);
	}

	public String nodeBuilder(ProvenanceType nodeType) {
		if (nodeType == null) {
			return "(n) ";
		}
		String nodeTypeAbbr = returnNodeAbbr(nodeType);
		return String.format("('%s':'%s')", nodeTypeAbbr, nodeType);
	}

	public List<ProvenanceEdge> formattedEdges(Graph graph) {
		List<ProvenanceEdge> edges = new ArrayList<>();
		for (Relationship relationship : graph.getRelationships()) {
			try {
				String startId = relationship.startNodeElementId();
				String endId = relationship.endNodeElementId();
				Node startNode = graph.getNodesById().get(startId);
				Node endNode = graph.getNodesById().get(endId);
				String startLabel = startNode.labels().iterator().next();
				String endLabel = endNode.labels().iterator().next();

				ProvenanceNode left = new ProvenanceNode();
				left.setId(UUID.fromString(startId));
				left.setType(ProvenanceType.findByType(startLabel));

				ProvenanceNode right = new ProvenanceNode();
				right.setId(UUID.fromString(endId));
				right.setType(ProvenanceType.findByType(endLabel));

				ProvenanceEdge edge = new ProvenanceEdge();
				edge.setRelationType(ProvenanceRelationType.findByType(relationship.type()));
				edge.setId(UUID.fromString(relationship.elementId()));
				edge.setLeft(left);
				edge.setRight(right);

				edges.add(edge);
			} catch (NoSuchElementException e) {
				log.warn("No element found: " + e);
				continue;
			}
		}
		return edges;
	}

}
