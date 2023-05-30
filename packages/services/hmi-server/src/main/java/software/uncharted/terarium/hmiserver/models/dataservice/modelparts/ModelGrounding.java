package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class ModelGrounding {
	private Map<String, Object> identifiers;
	private Map<String, Object> context;
}

