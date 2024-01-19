package software.uncharted.terarium.hmiserver.models.dataservice.workflow;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.io.Serializable;


// Node definition in the workflow
// This is the graphical operation of the operation defined in operationType
@Data
@Accessors(chain = true)
@TSModel
public class WorkflowNode<S> implements Serializable {

	// Information
	private String id;
	private String displayName;
	private String workflowId;
	private WorkflowOperationTypes operationType;
	// Position on canvas
	private Number x;
	private Number y;
	private Number width;
	private Number height;
	// Current operator state
	private S state;// Internal state. For example chosen model, display color ... etc
	@TSOptional
	private WorkflowOutput<S> active;
	// I/O
	private WorkflowPort[] inputs;
	private WorkflowOutput<S>[] outputs;
	// Behaviour
	private OperatorStatus status;

}


