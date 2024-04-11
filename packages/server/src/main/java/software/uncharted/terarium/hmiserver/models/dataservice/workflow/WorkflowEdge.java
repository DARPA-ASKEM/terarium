package software.uncharted.terarium.hmiserver.models.dataservice.workflow;

import java.io.Serializable;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
public class WorkflowEdge implements Serializable {

  private String id;
  private String workflowId;
  private Position[] points;
  private WorkflowNode source;
  private String sourcePortId;
  private WorkflowNode target;
  private String targetPortId;
  private WorkflowDirection direction;

  public enum WorkflowDirection {
    FROM_INPUT,
    FROM_OUTPUT
  }
}
