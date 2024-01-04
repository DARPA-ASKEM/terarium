package software.uncharted.terarium.hmiserver.models.dataservice.workflow;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@TSModel
public class WorkflowNode<T> implements Serializable {


	private String id;
	private String displayName;
	private String workflowId;
	private WorkflowOperationTypes operationType;
	private Number x;
	private Number y;
	private Number width;
	private Number height;
	private T state;
	@TSOptional
	private WorkflowOutput<T> active;
	private WorkflowPort[] inputs;
	private WorkflowOutput<T>[] outputs;
	private OperatorStatus status;

}


