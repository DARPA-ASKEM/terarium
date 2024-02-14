package software.uncharted.terarium.hmiserver.models.dataservice.petrinet;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelGrounding;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
public class PetriNetTransition extends SupportAdditionalProperties implements Serializable {
	@Serial
	private static final long serialVersionUID = -2008912624078705413L;

	private String id;

	private List<String> input;

	private List<String> output;

	@TSOptional
	private ModelGrounding grounding;

	private PetriNetTransitionProperties properties;
}
