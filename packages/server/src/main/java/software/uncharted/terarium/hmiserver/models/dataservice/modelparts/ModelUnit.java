package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@EqualsAndHashCode(callSuper = true)
@AMRSchemaType
@Accessors(chain = true)
public class ModelUnit extends SupportAdditionalProperties implements Serializable {

	@Serial
	private static final long serialVersionUID = 2545917939916234517L;

	private String expression;

	@JsonProperty("expression_mathml")
	private String expressionMathml;

	@Override
	public ModelUnit clone() {
		ModelUnit clone = (ModelUnit) super.clone();
		clone.setExpression(this.getExpression());
		clone.setExpressionMathml(this.getExpressionMathml());
		return clone;
	}
}
