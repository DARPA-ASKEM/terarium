package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.util.List;

@Data
@Accessors(chain = true)
public class Observable {
	private String id;

	@TSOptional
	private String name;

	@TSOptional
	private List<String> states;

	@TSOptional
	private String expression;

	@TSOptional
	@JsonAlias("expression_mathml")
	private String expression_mathml;
}
