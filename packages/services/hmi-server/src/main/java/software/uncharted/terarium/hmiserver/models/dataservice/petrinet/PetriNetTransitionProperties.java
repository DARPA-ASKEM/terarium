package software.uncharted.terarium.hmiserver.models.dataservice.petrinet;

import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelGrounding;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelExpression;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PetriNetTransitionProperties {
	private String name;
	private ModelGrounding grounding;
	private ModelExpression rate;
}

