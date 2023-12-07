package software.uncharted.terarium.hmiserver.service.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

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

	private Neo4jService neo4jService;

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
				formatted.setId(node.elementId());
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
				return String.format("-[%s]-", relationshipType);
			case "child":
				return String.format("<-[%s]-", relationshipType);
			case "parent":
				return String.format("-[%s]->", relationshipType);
			default:
				throw new IllegalArgumentException("Relationship direction is not allowed.");
		}
	}

	public String extractedModelsQueryGenerator(String rootType, String rootId) {
		switch (rootType) {
			case "Document":
				return String.format("MATCH (m:Model)-[:EXTRACTED_FROM]->(d:Document {id:'%s'}) RETURN m", rootId);
			case "Code":
				return String.format("MATCH (m:Model)-[:EXTRACTED_FROM]->(c:Code {id:'%s'}) RETURN m", rootId);
			case "Equation":
				return String.format("MATCH (m:Model)-[:EXTRACTED_FROM]->(e:Equation {id:'%s'}) RETURN m", rootId);
			default:
				throw new IllegalArgumentException("Models cannot be derived from this type: " + rootType);
		}
	}

	public String parentModelQueryGenerator(ProvenanceType rootType, String rootId) {
		String matchNode = matchNodeBuilder(rootType, rootId);
		String relationshipsStr = relationshipsArrayAsStr(
				Arrays.asList(ProvenanceRelationType.CONTAINS, ProvenanceRelationType.IS_CONCEPT_OF),
				new ArrayList<>());
		String modelRevisionNode = nodeBuilder(ProvenanceType.MODEL);

		Map<ProvenanceType, String> queryTemplatesIndex = new HashMap<>();
		queryTemplatesIndex.put(ProvenanceType.MODEL, String.format("-[r:BEGINS_AT]->%s ", modelRevisionNode));
		queryTemplatesIndex.put(ProvenanceType.MODEL_CONFIGURATION, String.format("-[r:USES]->%s ", modelRevisionNode));
		queryTemplatesIndex.put(ProvenanceType.SIMULATION,
				String.format("-[r:%s *1..]->%s ", relationshipsStr, modelRevisionNode));
		queryTemplatesIndex.put(ProvenanceType.DATASET,
				String.format("-[r:%s *1..]->%s ", relationshipsStr, modelRevisionNode));

		return matchNode + queryTemplatesIndex.get(rootType);
	}

	public String matchNodeBuilder(ProvenanceType nodeType, String nodeId) {
		if (nodeType == null) {
			return "MATCH (n) ";
		}
		String nodeTypeCharacter = returnNodeAbbr(nodeType);
		if (nodeId == null) {
			return String.format("MATCH (%s:%s)", nodeTypeCharacter, nodeType);
		}
		return String.format("MATCH (%s:%s {id: '%s'}) ", nodeTypeCharacter, nodeType, nodeId);
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
		return String.format("(%s:%s {id: '%s'}) ", nodeTypeAbbr, nodeType, nodeId);
	}

	public String nodeBuilder(ProvenanceType nodeType) {
		if (nodeType == null) {
			return "(n) ";
		}
		String nodeTypeAbbr = returnNodeAbbr(nodeType);
		return String.format("(%s:%s)", nodeTypeAbbr, nodeType);
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
				left.setId(startId);
				left.setType(ProvenanceType.findByType(startLabel));

				ProvenanceNode right = new ProvenanceNode();
				right.setId(endId);
				right.setType(ProvenanceType.findByType(endLabel));

				ProvenanceEdge edge = new ProvenanceEdge();
				edge.setRelationType(ProvenanceRelationType.findByType(relationship.type()));
				edge.setId(relationship.elementId());
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
