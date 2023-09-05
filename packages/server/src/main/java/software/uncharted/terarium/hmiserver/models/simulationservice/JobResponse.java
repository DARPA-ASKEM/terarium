package software.uncharted.terarium.hmiserver.models.simulationservice;

import lombok.Data;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonAlias;

@Data
@Accessors(chain = true)
public class JobResponse {
	@JsonAlias("simulation_id")
	private String simulationId;
}
