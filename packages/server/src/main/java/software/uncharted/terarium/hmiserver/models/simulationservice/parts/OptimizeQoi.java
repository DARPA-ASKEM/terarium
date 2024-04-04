package software.uncharted.terarium.hmiserver.models.simulationservice.parts;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
@TSModel
// Used to kick off a Optimize job in simulation-service
public class OptimizeQoi implements Serializable {
	private List<String> contexts;
	private String method;
}
