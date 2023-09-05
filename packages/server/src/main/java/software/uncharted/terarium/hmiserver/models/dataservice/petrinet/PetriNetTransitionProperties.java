package software.uncharted.terarium.hmiserver.models.dataservice.petrinet;

import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelGrounding;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

@Data
@Accessors(chain = true)
public class PetriNetTransitionProperties {
	private String name;
	private String description;

	@TSOptional
	private ModelGrounding grounding;
}

