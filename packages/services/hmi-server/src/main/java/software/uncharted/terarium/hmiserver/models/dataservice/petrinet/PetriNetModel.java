package software.uncharted.terarium.hmiserver.models.dataservice.petrinet;

import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import lombok.Data;
import lombok.experimental.Accessors;

import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelParameter;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelMetadata;

import java.util.List;

@Data
@Accessors(chain = true)
@TSModel
public class PetriNetModel {
	private List<PetriNetState> states;
	private List<PetriNetTransition> transitions;

	@TSOptional
	private List<ModelParameter> parameters;

	@TSOptional
	private ModelMetadata metadata;
}
