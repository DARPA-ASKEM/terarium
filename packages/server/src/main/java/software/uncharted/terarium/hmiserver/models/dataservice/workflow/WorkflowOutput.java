package software.uncharted.terarium.hmiserver.models.dataservice.workflow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.sql.Timestamp;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TSModel
public class WorkflowOutput<S> extends WorkflowPort{
	@TSOptional
	private boolean isSelected;
	@TSOptional
	private OperatorStatus operatorStatus;

	private S state;
	@TSOptional
	private Timestamp timestamp;
}
