package software.uncharted.terarium.hmiserver.models.dataservice.petrinet;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelGrounding;

@Data
@Accessors(chain = true)
public class PetriNetTransition implements SupportAdditionalProperties {
	private String id;
	private List<String> input;
	private List<String> output;

	@TSOptional
	private ModelGrounding grounding;

	private PetriNetTransitionProperties properties;
}
