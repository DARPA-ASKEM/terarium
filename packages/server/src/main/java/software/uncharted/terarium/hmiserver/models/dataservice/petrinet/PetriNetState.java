package software.uncharted.terarium.hmiserver.models.dataservice.petrinet;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelExpression;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelGrounding;

@Data
@Accessors(chain = true)
public class PetriNetState {
	private String id;

	private String name;

	private ModelGrounding grounding;

	private ModelExpression initial;
}
