package software.uncharted.terarium.hmiserver.models.dataservice.petrinet;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PetriNetState {
	private String id;
	private String name;

}

