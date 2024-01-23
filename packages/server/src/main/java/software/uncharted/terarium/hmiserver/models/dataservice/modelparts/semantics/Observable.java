package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@Accessors(chain = true)
public class Observable extends SupportAdditionalProperties {
	private String id;

	@TSOptional
	private String name;

	@TSOptional
	private List<String> states;

	@TSOptional
	private String expression;

	@TSOptional
	private String expression_mathml;
}
