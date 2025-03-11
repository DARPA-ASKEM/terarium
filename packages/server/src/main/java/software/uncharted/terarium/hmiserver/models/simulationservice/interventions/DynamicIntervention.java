package software.uncharted.terarium.hmiserver.models.simulationservice.interventions;

import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@TSModel
public class DynamicIntervention {

	private String parameter;
	private Number threshold;
	private Number value;
	private String appliedTo;
	private InterventionSemanticType type;
	private InterventionValueType valueType;

	@Override
	public DynamicIntervention clone() {
		DynamicIntervention clone = new DynamicIntervention();
		clone.parameter = this.parameter;
		clone.threshold = this.threshold;
		clone.value = this.value;
		clone.appliedTo = this.appliedTo;
		clone.type = this.type;
		clone.valueType = this.valueType;
		return clone;
	}
}
