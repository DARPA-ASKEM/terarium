package software.uncharted.terarium.hmiserver.models.dataservice.petrinet;

import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelGrounding;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class PetriNetTransition {
	private String id;
	private List<String> input;
	private List<String> output;
	private ModelGrounding grounding;
  private PetriNetTransitionProperties properties;
}

