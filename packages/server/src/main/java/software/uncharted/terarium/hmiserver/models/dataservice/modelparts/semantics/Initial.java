package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@EqualsAndHashCode(callSuper = true)
@AMRSchemaType
@Accessors(chain = true)
public class Initial extends SupportAdditionalProperties implements Serializable {

	@Serial
	private static final long serialVersionUID = 492480233470286509L;

	private String target;

	@TSOptional
	private String description;

	private String expression;

	@JsonProperty("expression_mathml")
	private String expressionMathml;

	@Override
	public Initial clone() {
		Initial clone = (Initial) super.clone();
		clone.setTarget(target);
		clone.setDescription(description);
		clone.setExpression(expression);
		clone.setExpressionMathml(expressionMathml);
		return clone;
	}
}
