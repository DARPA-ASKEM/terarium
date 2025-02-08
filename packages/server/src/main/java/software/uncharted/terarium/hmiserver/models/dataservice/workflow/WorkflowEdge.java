package software.uncharted.terarium.hmiserver.models.dataservice.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSIgnore;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class WorkflowEdge extends SupportAdditionalProperties implements Serializable {

	private UUID id;
	private UUID workflowId;

	@TSIgnore
	private Long version;

	private String createdBy;
	private Long createdAt;

	private Boolean isDeleted;

	private UUID source;
	private UUID sourcePortId;

	private UUID target;
	private UUID targetPortId;

	private List<JsonNode> points;

	public WorkflowEdge clone(final UUID workflowId, final UUID source, final UUID target) {
		final WorkflowEdge clone = (WorkflowEdge) super.clone();
		clone.setId(UUID.randomUUID());
		clone.setWorkflowId(workflowId);
		clone.setSource(source);
		clone.setSourcePortId(sourcePortId);
		clone.setTarget(target);
		clone.setTargetPortId(targetPortId);

		clone.setPoints(new ArrayList<>());
		if (points != null) {
			for (final JsonNode d : points) {
				clone.getPoints().add(d.deepCopy());
			}
		}
		return clone;
	}

	// Used for branching
	@Override
	public WorkflowEdge clone() {
		final WorkflowEdge clone = (WorkflowEdge) super.clone();
		clone.setId(id);
		clone.setWorkflowId(workflowId);
		clone.setSource(source);
		clone.setSourcePortId(sourcePortId);
		clone.setTarget(target);
		clone.setTargetPortId(targetPortId);

		clone.setPoints(new ArrayList<>());
		if (points != null) {
			for (final JsonNode d : points) {
				clone.getPoints().add(d.deepCopy());
			}
		}
		return clone;
	}

	public boolean getIsDeleted() {
		if (this.isDeleted == null) return false;

		return this.isDeleted;
	}
}
