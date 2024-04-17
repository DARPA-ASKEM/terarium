package software.uncharted.terarium.hmiserver.models.dataservice.workflow;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.JsonConverter;

/**
 * The workflow data structure is not very well defined. It is also meant to carry operations each with their own unique
 * representations. As such this is just a pass-thru class for the proxy. The UI has it's own typinging definition that
 * is not generated.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Entity
public class Workflow extends TerariumAsset {

	@Serial
	private static final long serialVersionUID = -1565930053830366145L;

	@Schema(defaultValue = "My New Workflow")
	private String name;

	private String description;

	private Transform transform;

	@Convert(converter = JsonConverter.class)
	@JdbcTypeCode(SqlTypes.JSON)
	private List<WorkflowNode> nodes;

	@Convert(converter = JsonConverter.class)
	@JdbcTypeCode(SqlTypes.JSON)
	private List<WorkflowEdge> edges;

	public Workflow clone() {
		final Workflow clone = new Workflow();

		cloneSuperFields(clone);

		clone.name = this.name;
		clone.description = this.description;
		if (this.transform != null) {
			clone.transform = new Transform()
					.setX(this.transform.getX())
					.setY(this.transform.getY())
					.setK(this.transform.getK());
		}

		final Map<UUID, UUID> oldToNew = new HashMap<>();

		clone.setNodes(new ArrayList<>());
		for (final WorkflowNode node : nodes) {
			final WorkflowNode clonedNode = node.clone(clone.getId());
			oldToNew.put(node.getId(), clonedNode.getId());
			clone.getNodes().add(clonedNode);
		}

		clone.setEdges(new ArrayList<>());
		for (final WorkflowEdge edge : edges) {
			final UUID clonedSourceId = oldToNew.get(edge.getSource());
			final UUID clonedTargetId = oldToNew.get(edge.getTarget());
			final WorkflowEdge clonedEdge = edge.clone(clone.getId(), clonedSourceId, clonedTargetId);
			clone.getEdges().add(clonedEdge);
		}
		return clone;
	}
}
