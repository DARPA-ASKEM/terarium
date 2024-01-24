package software.uncharted.terarium.hmiserver.models.dataservice.petrinet;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@Accessors(chain = true)
@TSModel
public class PetriNetModel implements SupportAdditionalProperties {
	private List<PetriNetState> states;
	private List<PetriNetTransition> transitions;

	// @TSOptional
	// private List<ModelParameter> parameters;

	// @TSOptional
	// private ModelMetadata metadata;
}
