package software.uncharted.terarium.hmiserver.models.dataservice.multiphysics;

import java.io.Serializable;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
public class ContextConstant implements Serializable {
	private String _type;
	private Object value;
}
