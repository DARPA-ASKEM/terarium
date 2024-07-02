package software.uncharted.terarium.hmiserver.models.simulationservice.interventions;

import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@TSModel
public class StaticIntervention {
	private Number threshold;
	private Number value;

	@Override
	public StaticIntervention clone() {
		StaticIntervention staticIntervention = new StaticIntervention();
		staticIntervention.threshold = this.threshold;
		staticIntervention.value = this.value;
		return staticIntervention;
	}
}
