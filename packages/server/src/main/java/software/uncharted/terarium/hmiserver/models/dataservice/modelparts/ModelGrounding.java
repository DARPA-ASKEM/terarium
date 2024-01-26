package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@AMRSchemaType
@Accessors(chain = true)
public class ModelGrounding implements SupportAdditionalProperties {
	private Map<String, Object> identifiers;

	@TSOptional
	private Map<String, Object> context;

	@TSOptional
	private Object modifiers;
}
