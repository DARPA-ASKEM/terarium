package software.uncharted.terarium.hmiserver.models.dataservice.workflow;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@TSModel
public class WorkflowEdge implements Serializable {

	private String id;
	private String workflowId;
	private Position[] points;
	@TSOptional
	private String source;
	@TSOptional
	private String sourcePortId;
	@TSOptional
	private String target;
	@TSOptional
	private String targetPortId;
	@TSOptional
	private WorkflowDirection direction;

	public enum WorkflowDirection {
		FROM_INPUT,
		FROM_OUTPUT
	}
}
