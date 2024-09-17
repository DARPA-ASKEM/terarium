package software.uncharted.terarium.hmiserver.models.simulationservice.parts;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
// Used to specify the location of a dataset for simulation-service
public class TimeSpan {

	private Double start;
	private Double end;
}
