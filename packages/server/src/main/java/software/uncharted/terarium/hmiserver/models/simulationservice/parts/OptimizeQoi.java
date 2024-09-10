package software.uncharted.terarium.hmiserver.models.simulationservice.parts;

import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
// Used to kick off a Optimize job in simulation-service
public class OptimizeQoi implements Serializable {

	private List<String> contexts;
	private String method;
}
