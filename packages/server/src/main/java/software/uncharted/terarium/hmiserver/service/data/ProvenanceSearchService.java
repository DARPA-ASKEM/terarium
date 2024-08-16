package software.uncharted.terarium.hmiserver.service.data;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.Provenance;
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

	/**
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
				new ArrayList<>()
			);

			final String limit = payload.getLimit() != null ? "limit: " + payload.getLimit() + ", " : "";
			final String hops = payload.getHops() != null ? "maxLevel: " + payload.getHops() + ", " : "";

			final String query =
				matchNode +
				" CALL apoc.path.subgraphAll(" +
				nodeAbbr +
				", {" +
				"relationshipFilter: '" +
				relationshipsStr +
				"'," +
				"minLevel: 0, " +
				limit +
				hops +
				"whitelistNodes: []" +
				"}) YIELD nodes, relationships RETURN nodes, relationships";

			final Result result = session.run(query);

			return nodesEdges(result, payload);
		}
	}

	/**
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
				matchNode,
				payload.getCurie()
			);

			final Result response = session.run(query);

			return nodesEdges(response, payload);
		}
	}

	/**
	 * Get the documents and datasets used to create a model configuration
	 *
	 * @param id - Model configuration id.
	 * @return Set<Provenance> - The Documents and Datasets used to create a model configuration.
	 */
	public Set<Provenance> modelConfigSource(final UUID id) {
		final Set<Provenance> provenances = new HashSet<>();
		try (final Session session = neo4jService.getSession()) {
			final String query = String.format(
				"MATCH (d:Document)<-[r:EXTRACTED_FROM]-(m:ModelConfiguration {id: '%s'}) RETURN d.id as id, 'DOCUMENT' as type " +
				"UNION " +
				"MATCH (ds:Dataset)<-[r:EXTRACTED_FROM]-(m:ModelConfiguration {id: '%s'}) RETURN ds.id as id, 'DATASET' as type",
				id,
				id
			);

			final Result response = session.run(query);
			while (response.hasNext()) {
				final var record = response.next();
				final ProvenanceType recordType = record.get("type").asString().equals("DOCUMENT")
					? ProvenanceType.DOCUMENT
					: ProvenanceType.DATASET;
				final Provenance provenance = new Provenance();
				provenance.setLeft(id);
				provenance.setLeftType(ProvenanceType.MODEL_CONFIGURATION);
				provenance.setRight(UUID.fromString(record.get("id").asString()));
				provenance.setRightType(recordType);
				provenance.setRelationType(ProvenanceRelationType.EXTRACTED_FROM);
				provenances.add(provenance);
			}
			return provenances;
		}
	}

	/**
	 * Identifies the document from which a model was extracted
	 *
	 * @param payload - Search param payload.
	 * @return
	 */
	public Set<String> modelsFromDocument(final ProvenanceQueryParam payload) {
		if (payload.getRootType() != ProvenanceType.MODEL) {
			throw new IllegalArgumentException("Document used for model extraction can only be found by providing a Model");
		}

		try (final Session session = neo4jService.getSession()) {
			final UUID modelId = payload.getRootId();

			final String query = String.format(
				"MATCH (d:Document)<-[r:EXTRACTED_FROM]-(m:Model {id: '%s'}) RETURN d",
				modelId
			);

			final Result response = session.run(query);

			final Set<String> responseData = new HashSet<>();
			while (response.hasNext()) {
				responseData.add(response.next().get("d").get("id").asString());
			}

			return responseData;
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

	public static void modelRevisionsToModel(
		final ProvenanceNode model,
		final ProvenanceNode modelRevision,
		final List<ProvenanceEdge> edges
	) {
		for (final ProvenanceEdge edge : edges) {
			if (edge.getRight().equals(modelRevision)) {
				edge.setRight(model);
				final ProvenanceNode left = edge.getLeft();
				if (
					left.getType() == ProvenanceType.MODEL_REVISION &&
					ProvenanceRelationType.EDITED_FROM == edge.getRelationType()
				) {
					modelRevisionsToModel(model, left, edges);
				}
			}

			if (edge.getLeft().equals(modelRevision)) {
				edge.setLeft(model);
				final ProvenanceNode right = edge.getRight();
				if (
					right.getType() == ProvenanceType.MODEL_REVISION &&
					ProvenanceRelationType.EDITED_FROM == edge.getRelationType()
				) {
					modelRevisionsToModel(model, right, edges);
				}
			}
		}
	}

	public static List<ProvenanceEdge> filterRelationshipTypes(
		final List<ProvenanceEdge> relationships,
		final List<ProvenanceType> includedTypes
	) {
		if (includedTypes.isEmpty()) {
			return relationships;
		}
		final List<ProvenanceEdge> clipped = new ArrayList<>();
		for (final ProvenanceEdge relation : relationships) {
			final ProvenanceNode left = relation.getLeft();
			final ProvenanceNode right = relation.getRight();
			if (!left.equals(right) && includedTypes.contains(left.getType()) && includedTypes.contains(right.getType())) {
				clipped.add(relation);
			}
		}
		return clipped;
	}

	public static List<ProvenanceNode> filterNodeTypes(
		final List<ProvenanceNode> nodes,
		final List<ProvenanceType> includedTypes
	) {
		if (includedTypes.isEmpty()) {
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
			}
		}
		return nodes;
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
			@Serial
			private static final long serialVersionUID = -2033583374684450222L;

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

	public static String relationshipsArrayAsStr(
		final List<ProvenanceRelationType> exclude,
		final List<ProvenanceRelationType> include
	) {
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
				edge.setLeft(left);
				edge.setRight(right);

				edges.add(edge);
			} catch (final NoSuchElementException e) {
				log.warn("No element found: " + e);
			}
		}
		return edges;
	}
}
