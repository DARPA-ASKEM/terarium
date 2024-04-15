package software.uncharted.terarium.hmiserver.models.dataservice.petrinet;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelExpression;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelGrounding;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class PetriNetState extends SupportAdditionalProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 4168200741563747279L;

    private String id;

    private String name;

    private ModelGrounding grounding;

    private ModelExpression initial;
}
