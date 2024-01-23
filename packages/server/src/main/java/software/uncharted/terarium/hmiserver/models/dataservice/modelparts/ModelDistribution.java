package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@Accessors(chain = true)
public class ModelDistribution extends SupportAdditionalProperties {
	private String type;
	private Map<String, Object> parameters;
}
