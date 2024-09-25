package software.uncharted.terarium.hmiserver.models.simulationservice.interventions;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@TSModel
public class Intervention {

	private String name;
	private List<StaticIntervention> staticInterventions;
	private List<DynamicIntervention> dynamicInterventions;

	@Override
	public Intervention clone() {
		Intervention intervention = new Intervention();
		intervention.setName(name);

		if (staticInterventions != null) {
			intervention.setStaticInterventions(new ArrayList<>());
			for (StaticIntervention staticIntervention : staticInterventions) {
				intervention.getStaticInterventions().add(staticIntervention.clone());
			}
		}
		if (dynamicInterventions != null) {
			intervention.setDynamicInterventions(new ArrayList<>());
			for (DynamicIntervention dynamicIntervention : dynamicInterventions) {
				intervention.getDynamicInterventions().add(dynamicIntervention.clone());
			}
		}
		return intervention;
	}
}
