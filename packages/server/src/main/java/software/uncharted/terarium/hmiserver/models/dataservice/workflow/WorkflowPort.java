package software.uncharted.terarium.hmiserver.models.dataservice.workflow;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
@TSModel
public class WorkflowPort implements Serializable {

	private String id;
	private String type;
	private WorkflowPortStatus status;
	@TSOptional
	private String label;
	@TSOptional
	private List<Object> value;
	private boolean isOptional;
	@TSOptional
	private boolean acceptMultiple;
}

