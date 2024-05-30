package software.uncharted.terarium.hmiserver.models.dataservice.petrinet;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelGrounding;

@Data
@EqualsAndHashCode(callSuper = true)
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
