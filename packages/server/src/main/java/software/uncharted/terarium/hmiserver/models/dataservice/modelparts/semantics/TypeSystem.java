package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

import java.util.List;

@Data
@Accessors(chain = true)
@TSModel
public class TypeSystem {

	private List<State> states;
	private List<Transition> transitions;
}
