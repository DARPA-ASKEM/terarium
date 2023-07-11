package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

@Data
@Accessors(chain = true)
public class Observables {
	private String id;

	@TSOptional
	private String name;

	@TSOptional
	private String expression;

	@TSOptional
	private String expression_mathml;
}
