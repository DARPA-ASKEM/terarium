package software.uncharted.terarium.hmiserver.models.dataservice.workflow;

import java.io.Serializable;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

@Data
@Accessors(chain = true)
@TSModel
public class WorkflowNode<S> implements Serializable {

  private String id;
  private String displayName;
  private String workflowId;
  private WorkflowOperationTypes operationType;
  private Number x;
  private Number y;
  private Number width;
  private Number height;
  private S state;
  @TSOptional private WorkflowOutput<S> active;
  private WorkflowPort[] inputs;
  private WorkflowOutput<S>[] outputs;
  private OperatorStatus status;
}
