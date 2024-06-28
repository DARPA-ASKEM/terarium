package software.uncharted.terarium.hmiserver.models.simulationservice.interventions;

import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@TSModel
public class DynamicIntervention {

	private String parameter;
	private Number threshold;
	private Number value;

}
