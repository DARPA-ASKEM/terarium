package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelGrounding;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelUnit;

@Data
@EqualsAndHashCode(callSuper = true)
@AMRSchemaType
@Accessors
public class Observable extends SupportAdditionalProperties implements Serializable, GroundedSemantic {

	@Serial
	private static final long serialVersionUID = -8367278228176339223L;

	private String id;

	@TSOptional
	private String name;

	@TSOptional
	private List<String> states;

	@TSOptional
	private String description;

	@TSOptional
	private ModelUnit units;

	@TSOptional
	private String expression;

	@TSOptional
	private ModelGrounding grounding;

	@TSOptional
	@JsonProperty("expression_mathml")
	private String expressionMathml;

	@Override
	public Observable clone() {
		Observable clone = (Observable) super.clone();

		clone.setId(this.getId());
		clone.setName(this.getName());
		if (this.states != null) {
			clone.setStates(this.getStates());
		}
		clone.setExpression(this.getExpression());
		clone.setExpressionMathml(this.getExpressionMathml());

		return clone;
	}
}
