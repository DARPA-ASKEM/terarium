package software.uncharted.terarium.hmiserver.models.simulationservice.interventions;

import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@TSModel
public class StaticIntervention {

	private Number timestep;
	private Number value;
	private String appliedTo;
	private InterventionSemanticType type;

	@Override
	public StaticIntervention clone() {
		StaticIntervention staticIntervention = new StaticIntervention();
		staticIntervention.timestep = this.timestep;
		staticIntervention.value = this.value;
		staticIntervention.appliedTo = this.appliedTo;
		staticIntervention.type = this.type;
		return staticIntervention;
	}
}
