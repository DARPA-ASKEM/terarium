package software.uncharted.terarium.hmiserver.models.modelservice;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class StratifyRequest implements Serializable {
	private Object baseModel;
	private Object strataModel;
}
