package software.uncharted.terarium.hmiserver.models.simulationservice.interventions;

import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@TSModel
public class StaticIntervention {
	private Number threshold;
	private Number value;
}
