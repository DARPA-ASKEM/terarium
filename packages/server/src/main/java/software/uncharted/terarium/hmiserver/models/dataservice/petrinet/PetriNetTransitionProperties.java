package software.uncharted.terarium.hmiserver.models.dataservice.petrinet;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelGrounding;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class PetriNetTransitionProperties extends SupportAdditionalProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -4641087025832077991L;

    private String name;

    private String description;

    @TSOptional
    private ModelGrounding grounding;
}
