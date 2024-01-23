package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@Accessors(chain = true)
public class ModelGrounding extends SupportAdditionalProperties {
	private Map<String, Object> identifiers;

	@TSOptional
	private Map<String, Object> context;

	@TSOptional
	private Object modifiers;
}
