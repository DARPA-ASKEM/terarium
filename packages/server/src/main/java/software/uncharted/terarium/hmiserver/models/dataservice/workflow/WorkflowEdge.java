package software.uncharted.terarium.hmiserver.models.dataservice.workflow;

import java.io.Serializable;
import java.util.UUID;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TSModel
public class WorkflowEdge extends SupportAdditionalProperties implements Serializable {

	private UUID id;
	private UUID workflowId;
	private WorkflowNode source;
	private WorkflowNode target;

	public WorkflowEdge clone(final UUID workflowId, final WorkflowNode source, final WorkflowNode target) {
		final WorkflowEdge clone = new WorkflowEdge();
		clone.setId(UUID.randomUUID());
		clone.setWorkflowId(workflowId);
		clone.setSource(source);
		clone.setTarget(target);
		clone.setAdditionalProperties(this.getAdditionalProperties());
		return clone;
	}
}
