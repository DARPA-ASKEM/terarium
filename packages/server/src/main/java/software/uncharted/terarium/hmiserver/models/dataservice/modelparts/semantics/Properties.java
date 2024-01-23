package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelGrounding;

@Data
@Accessors(chain = true)
public class Properties extends SupportAdditionalProperties {
	private String name;

	@TSOptional
	private ModelGrounding grounding;

	@TSOptional
	private String description;
}
