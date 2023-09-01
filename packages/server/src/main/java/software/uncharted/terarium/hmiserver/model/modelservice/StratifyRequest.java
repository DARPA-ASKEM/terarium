package software.uncharted.terarium.hmiserver.model.modelservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class StratifyRequest implements Serializable {
	private Object baseModel;
	private Object strataModel;
}
