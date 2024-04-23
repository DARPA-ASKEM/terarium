package software.uncharted.terarium.hmiserver.models.dataservice.workflow;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Where;
import org.hibernate.type.SqlTypes;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.ObjectConverter;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.Simulation;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Entity
public class Workflow extends TerariumAsset {

	@Serial
	private static final long serialVersionUID = -1565930053830366145L;

	private String description;

	private Transform transform;

	@Convert(converter = ObjectConverter.class)
	@JdbcTypeCode(SqlTypes.JSON)
	private List<WorkflowNode> nodes;

	@Convert(converter = ObjectConverter.class)
	@JdbcTypeCode(SqlTypes.JSON)
	private List<WorkflowEdge> edges;

	@OneToOne(mappedBy = "workflow")
	@Where(clause = "deleted_on IS NULL")
	@ToString.Exclude
	@JsonManagedReference
	private Simulation simulation;

	@Override
	public Workflow clone() {
		final Workflow clone = new Workflow();

		cloneSuperFields(clone);

		if(simulation != null) {
			clone.setSimulation(simulation.clone());
		}

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
