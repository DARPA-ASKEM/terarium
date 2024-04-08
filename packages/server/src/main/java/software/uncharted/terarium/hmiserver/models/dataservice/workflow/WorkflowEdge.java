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
	private WorkflowNode source;
	private WorkflowNode target;

	public WorkflowEdge clone() {
		final WorkflowEdge clone = new WorkflowEdge();
		clone.setId(UUID.randomUUID());
		clone.setSource(null); // these are set by Workflow
		clone.setTarget(null); // these are set by Workflow
		clone.setAdditionalProperties(this.getAdditionalProperties());
		return clone;
	}
}
