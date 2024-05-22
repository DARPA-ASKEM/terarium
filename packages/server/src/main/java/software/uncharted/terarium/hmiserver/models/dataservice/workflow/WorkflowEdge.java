package software.uncharted.terarium.hmiserver.models.dataservice.workflow;

import java.io.Serializable;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class WorkflowEdge extends SupportAdditionalProperties implements Serializable {

	private UUID id;
	private UUID workflowId;
	private UUID source;
	private UUID target;

	public WorkflowEdge clone(final UUID workflowId, final UUID source, final UUID target) {
		final WorkflowEdge clone = (WorkflowEdge) super.clone();
		clone.setId(UUID.randomUUID());
		clone.setWorkflowId(workflowId);
		clone.setSource(source);
		clone.setTarget(target);
		return clone;
	}
}
