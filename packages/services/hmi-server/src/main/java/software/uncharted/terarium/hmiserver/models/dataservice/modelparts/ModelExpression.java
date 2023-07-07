package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ModelExpression {
	private String expression;

	private String expression_mathml;
}

