package software.uncharted.terarium.hmiserver.models.simulationservice.parts;

import java.io.Serializable;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
// Used to kick off a Optimize job in simulation-service
public class OptimizeQoi implements Serializable {

	private String context;
	private String method;
}
