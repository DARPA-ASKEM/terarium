package software.uncharted.terarium.hmiserver.models.dataservice.workflow;

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;

/**
 * The workflow data structure is not very well defined. It is also meant to
 * carry operations each with their own unique
 * representations. As such this is just a pass-thru class for the proxy. The UI
 * has it's own typinging definition that is
 * not generated.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TSModel
@Entity
public class Workflow extends TerariumAsset {

	@Serial
	private static final long serialVersionUID = -1565930053830366145L;

	@Schema(defaultValue = "My New Workflow")
	private String name;

	private String description;

	private Transform transform;

	@JdbcTypeCode(SqlTypes.JSON)
	private List<WorkflowNode> nodes;

	@JdbcTypeCode(SqlTypes.JSON)
	private List<WorkflowEdge> edges;

	public Workflow clone() {
		final Workflow clone = new Workflow();
		clone.setName(this.getName());
		clone.setDescription(this.getDescription());
		clone.setTransform(this.getTransform());
		clone.setNodes(new ArrayList<>());

		final Map<UUID, UUID> oldToNew = new HashMap<>();

		for (final WorkflowNode node : nodes) {
			final WorkflowNode clonedNode = node.clone();
			oldToNew.put(node.getId(), clonedNode.getId());
			clone.getNodes().add(clonedNode);
		}

		clone.setEdges(new ArrayList<>());
		for (final WorkflowEdge edge : edges) {

			final WorkflowEdge clonedEdge = edge.clone();

			clonedEdge.getSource().setId(oldToNew.get(edge.getSource().getId()));
			clonedEdge.getTarget().setId(oldToNew.get(edge.getTarget().getId()));

			clone.getEdges().add(clonedEdge);
		}
		return clone;
	}

}
