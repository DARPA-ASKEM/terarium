package software.uncharted.terarium.hmiserver.models.data.workflow;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;


@Data
@Accessors(chain = true)
public class WorkflowTransform implements Serializable {

	@Serial
	private static final long serialVersionUID = 5420120419478777003L;

	private Long x;

	private Long y;

	private Long k;
}
