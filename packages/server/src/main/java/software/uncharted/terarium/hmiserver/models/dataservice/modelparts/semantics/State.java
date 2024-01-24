package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelGrounding;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelUnit;

@Data
@AMRSchemaType
@Accessors(chain = true)
@TSModel
public class State implements SupportAdditionalProperties {
	private String id;
	@TSOptional
	private String name;
	@TSOptional
	private String description;
	@TSOptional
	private ModelGrounding grounding;
	@TSOptional
	private ModelUnit units;
}
