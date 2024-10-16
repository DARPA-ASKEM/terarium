package software.uncharted.terarium.hmiserver.models.simulationservice.interventions;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Type;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;

/** Used to specify any interventions provided by the AMR and given to the simulation-service. */
@EqualsAndHashCode(callSuper = true)
@Data
@TSModel
@Accessors(chain = true)
@Entity
@Slf4j
public class InterventionPolicy extends TerariumAsset {

	private UUID modelId;

	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private List<Intervention> interventions;

	@Override
	public InterventionPolicy clone() {
		final InterventionPolicy clone = new InterventionPolicy();
		super.cloneSuperFields(clone);
		clone.setModelId(this.modelId);
		if (interventions != null) {
			clone.setInterventions(new ArrayList<>());
			for (final Intervention intervention : interventions) {
				clone.getInterventions().add(intervention.clone());
			}
		}
		return clone;
	}

	// Check each intervention this policy contains
	// If any of them are are invalid the entire policy is invalid.
	public Boolean validateInterventionPolicy() throws Exception {
		for (int i = 0; i < this.interventions.size(); i++) {
			this.interventions.get(i).validateIntervention(); //validate individual intervention
			for (int j = 0; j < this.interventions.size(); j++) {
				if (Integer.compare(i, j) != 0) {
					validateInterventionPair(this.interventions.get(i), this.interventions.get(j)); //validate pair with eachother
				}
			}
		}
		return true;
	}

	// Takes a list of two interventions and will check their static intervention lists to ensure
	// there are no duplicate time + appliedTo pairs in their indiviudal static interventions
	private Boolean validateInterventionPair(Intervention interOne, Intervention interTwo) throws Exception {
		List<StaticIntervention> staticOne = interOne.getStaticInterventions();
		List<StaticIntervention> staticTwo = interTwo.getStaticInterventions();

		for (int i = 0; i < staticOne.size(); i++) {
			for (int j = 0; j < staticTwo.size(); j++) {
				final String appliedToOne = staticOne.get(i).getAppliedTo();
				final Number timeOne = staticOne.get(i).getTimestep();
				final String appliedToTwo = staticTwo.get(j).getAppliedTo();
				final Number timeTwo = staticTwo.get(j).getTimestep();
				if (appliedToOne.equals(appliedToTwo) && timeOne.equals(timeTwo)) {
					final String errorMessage = String.format(
						"The intervention %s and %s have duplicate applied to: %s and time: %s pairs.",
						interOne.getName(),
						interTwo.getName(),
						appliedToOne,
						String.valueOf(timeOne)
					);
					throw new Exception(errorMessage);
				}
			}
		}
		return true;
	}
}
