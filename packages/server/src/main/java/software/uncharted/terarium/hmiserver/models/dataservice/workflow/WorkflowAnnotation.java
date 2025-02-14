package software.uncharted.terarium.hmiserver.models.dataservice.workflow;

import java.io.Serializable;
import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class WorkflowAnnotation implements Serializable {

	private UUID id;
	private String type;
	private Double x;
	private Double y;
	private Double textSize;
	private String content;
}
