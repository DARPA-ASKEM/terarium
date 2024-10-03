package software.uncharted.terarium.hmiserver.models.simulationservice.interventions;

import java.util.ArrayList;
import java.util.List;
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
	public Boolean validateIntervention() {
		for (int i = 0; i < this.staticInterventions.size(); i++) {
			if (this.staticInterventions.get(i).getTimestep().intValue() < 0) {
				log.warn(
					String.format(
						"The intervention % has a timestep % which is less than 0.",
						this.getName(),
						this.staticInterventions.get(i).getTimestep().toString()
					)
				);
				return false;
			}
			for (int j = 0; j < this.staticInterventions.size(); j++) {
				final String appliedToOne = this.staticInterventions.get(i).getAppliedTo();
				final Number timeOne = this.staticInterventions.get(i).getTimestep();
				final String appliedToTwo = this.staticInterventions.get(j).getAppliedTo();
				final Number timeTwo = this.staticInterventions.get(j).getTimestep();
				if (i != j && appliedToOne == appliedToTwo && timeOne == timeTwo) {
					log.warn(
						String.format(
							"The intervention % has duplicate applied to: % and time: % pairs.",
							this.getName(),
							appliedToOne,
							timeOne
						)
					);
					return false;
				}
			}
		}
		return true;
	}
}
