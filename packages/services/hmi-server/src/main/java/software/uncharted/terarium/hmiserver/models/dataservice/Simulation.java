package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.simulationservice.SimulationParams;

@Data
@Accessors(chain = true)
@TSModel
public class Simulation {
	@TSOptional
	private Long id;
	private String name;
	@TSOptional
	private String description;
	private SimulationParams simulationParams;
	@TSOptional
	private String result;
}
