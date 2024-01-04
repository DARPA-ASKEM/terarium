package software.uncharted.terarium.hmiserver.models.dataservice.workflow;

import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.sql.Timestamp;

public class WorkflowOutput<T> extends WorkflowPort{
	@TSOptional
	private boolean isSelected;
	@TSOptional
	private OperatorStatus operatorStatus;
	@TSOptional
	private T state;
	@TSOptional
	private Timestamp timestamp;
}
