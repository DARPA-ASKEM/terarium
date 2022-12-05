package software.uncharted.terarium.hmiserver.models.modelservice;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.modelservice.StateTransition;
import software.uncharted.terarium.hmiserver.models.modelservice.InputOutput;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class PetriNet implements Serializable {
    // private List<StateTransition> S;
	// private List<StateTransition> T;
    // private List<InputOutput> I;
	// private List<InputOutput> O;
	private List<Map<String, String>> S;
	private List<Map<String, String>> T;
    private List<Map<String, Integer>> I;
	private List<Map<String, Integer>> O;
}