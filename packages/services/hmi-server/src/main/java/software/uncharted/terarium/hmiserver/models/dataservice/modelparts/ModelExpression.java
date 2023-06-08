package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import lombok.Data;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonSetter;


@Data
@Accessors(chain = true)
public class ModelExpression {
	private String expression;

	@JsonAlias("expression_mathml")
	private String expressionMathml;
}

