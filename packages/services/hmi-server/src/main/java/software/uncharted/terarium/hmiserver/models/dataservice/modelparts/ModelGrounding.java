package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.util.Map;

@Data
@Accessors(chain = true)
public class ModelGrounding {
	private Map<String, Object> identifiers;
	@TSOptional
	private Map<String, Object> context;
}

