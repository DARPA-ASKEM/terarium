package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelGrounding;

import java.util.List;

@Data
@Accessors(chain = true)
@TSModel
public class Transition {
	private String id;
	private List<String> input;
	private List<String> output;
	@TSOptional
	private ModelGrounding grounding;
	@TSOptional
	private Properties properties;
}
