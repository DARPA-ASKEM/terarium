package software.uncharted.terarium.hmiserver.models.dataservice.petrinet;

import lombok.Data;
import lombok.experimental.Accessors;

import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelParameter;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelMetadata;

import java.util.List;

@Data
@Accessors(chain = true)
public class PetriNetModel {
	private List<PetriNetState> states;
	private List<PetriNetTransition> transitions;
	private List<ModelParameter> parameters;
	private ModelMetadata metadata;
}
