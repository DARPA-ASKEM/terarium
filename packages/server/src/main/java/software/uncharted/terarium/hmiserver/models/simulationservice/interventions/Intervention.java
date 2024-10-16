package software.uncharted.terarium.hmiserver.models.simulationservice.interventions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@TSModel
@Slf4j
public class Intervention {

	private String name;
	private List<StaticIntervention> staticInterventions = new ArrayList<>();
	private List<DynamicIntervention> dynamicInterventions = new ArrayList<>();

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

	// Check for no negative timesteps
	// Check for no duplicate time + appliedTo pairs in static interventions
	public Boolean validateIntervention() throws Exception {
		final Set<String> foo = new HashSet<>();
		for (int i = 0; i < this.staticInterventions.size(); i++) {
			final Number time = this.staticInterventions.get(i).getTimestep();
			if (time.doubleValue() < 0) {
				final String errorMessage = String.format(
					"The intervention %s has a timestep %s which is less than 0.",
					this.getName(),
					time.toString()
				);
				throw new Exception(errorMessage);
			}
			final String key =
				this.staticInterventions.get(i).getAppliedTo() + this.staticInterventions.get(i).getTimestep().toString();
			if (foo.contains(key)) {
				final String errorMessage = String.format(
					"The intervention %s has duplicate applied to: %s and time: %s pairs.",
					this.getName(),
					this.staticInterventions.get(i).getAppliedTo(),
					this.staticInterventions.get(i).getTimestep().toString()
				);
				throw new Exception(errorMessage);
			} else {
				foo.add(key);
			}
		}
		return true;
	}
}
