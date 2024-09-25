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
public class Rate extends SupportAdditionalProperties implements Serializable {

	@Serial
	private static final long serialVersionUID = -5200389942863765459L;

	private String target;

	private String expression;

	@TSOptional
	@JsonProperty("expression_mathml")
	private String expressionMathml = "";

	@Override
	public Rate clone() {
		final Rate clone = (Rate) super.clone();
		clone.setTarget(this.getTarget());
		clone.setExpression(this.getExpression());
		clone.setExpressionMathml(this.getExpressionMathml());
		return clone;
	}
}
