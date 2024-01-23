package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@Accessors(chain = true)
public class ModelUnit extends SupportAdditionalProperties {
	private String expression;

	private String expression_mathml;
}
