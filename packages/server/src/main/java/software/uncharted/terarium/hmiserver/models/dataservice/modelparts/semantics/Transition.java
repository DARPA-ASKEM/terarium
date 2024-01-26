package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelGrounding;

@Data
@AMRSchemaType
@Accessors(chain = true)
@TSModel
public class Transition implements SupportAdditionalProperties {
	private String id;
	private List<String> input;
	private List<String> output;
	@TSOptional
	private ModelGrounding grounding;
	@TSOptional
	private Properties properties;
}
