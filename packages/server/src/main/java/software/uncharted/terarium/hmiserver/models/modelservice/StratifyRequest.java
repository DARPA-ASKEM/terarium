package software.uncharted.terarium.hmiserver.models.modelservice;

import java.io.Serializable;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StratifyRequest implements Serializable {

	private Object baseModel;
	private Object strataModel;
}
