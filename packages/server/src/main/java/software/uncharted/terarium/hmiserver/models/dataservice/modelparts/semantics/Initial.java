package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Initial {
	private String target;
	private String expression;

	@JsonAlias("expression_mathml")
	private String expression_mathml;
}
