package software.uncharted.terarium.hmiserver.models.simulationservice.parts;

import com.fasterxml.jackson.annotation.JsonAlias;
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

	@JsonAlias("risk_bound")
	private Double riskBound;

	@JsonAlias("is_minimized")
	private Boolean isMinimized;
}
