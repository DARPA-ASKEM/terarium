package software.uncharted.terarium.hmiserver.models.dataservice.petrinet;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelGrounding;

import java.io.Serial;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class PetriNetTransitionProperties extends SupportAdditionalProperties implements Serializable {
	@Serial
	private static final long serialVersionUID = -4641087025832077991L;

	private String name;

	private String description;

	@TSOptional
	private ModelGrounding grounding;
}
