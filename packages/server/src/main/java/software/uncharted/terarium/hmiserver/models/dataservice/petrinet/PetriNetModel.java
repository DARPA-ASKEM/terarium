package software.uncharted.terarium.hmiserver.models.dataservice.petrinet;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@Accessors(chain = true)
@TSModel
public class PetriNetModel extends SupportAdditionalProperties implements Serializable {
  @Serial private static final long serialVersionUID = -6590800889694442630L;

  private List<PetriNetState> states;

  private List<PetriNetTransition> transitions;
}
