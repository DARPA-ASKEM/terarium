package software.uncharted.terarium.hmiserver.service.neo4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;

@Data
public class Graph {

	public Graph(final Result result) {
		nodes = new ArrayList<>();
		nodesById = new HashMap<>();
		relationships = new ArrayList<>();

		while (result.hasNext()) {
			final Record r = result.next();

			final List<Node> recordNodes = r.get("nodes").asList(Value::asNode);
			final List<Relationship> recordRelationships = r.get("relationships").asList(Value::asRelationship);

			nodes.addAll(recordNodes);
			relationships.addAll(recordRelationships);
		}

		for (final Node node : nodes) {
			nodesById.put(node.elementId(), node);
		}
	}

	private List<Node> nodes;
	private Map<String, Node> nodesById;
	private List<Relationship> relationships;
}
