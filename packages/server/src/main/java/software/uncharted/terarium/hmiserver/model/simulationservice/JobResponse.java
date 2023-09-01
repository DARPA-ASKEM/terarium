package software.uncharted.terarium.hmiserver.model.simulationservice;

import lombok.Data;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;

@Data
@Accessors(chain = true)
public class JobResponse {
	@JsonAlias("simulation_id")
	private String simulationId;
}
