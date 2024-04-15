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
public class WorkflowNode extends SupportAdditionalProperties implements Serializable {

    private UUID id;
    private UUID workflowId;

    public WorkflowNode clone(final UUID workflowId) {
        final WorkflowNode clone = new WorkflowNode();
        clone.setId(UUID.randomUUID());
        clone.setWorkflowId(workflowId);
        clone.setAdditionalProperties(this.getAdditionalProperties());
        return clone;
    }
}
