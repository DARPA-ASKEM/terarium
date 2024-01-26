package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@AMRSchemaType
@Accessors(chain = true)
public class ModelDistribution implements SupportAdditionalProperties {
	private String type;
	private Map<String, Object> parameters;
}
