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
	public Boolean validatePolicyIntervention() {
		for (int i = 0; i < this.interventions.size(); i++) {
			final boolean checkIndividual = this.interventions.get(i).validateIntervention();
			if (checkIndividual == false) {
				return false;
			}
			for (int j = 0; j < this.interventions.size(); j++) {
				if (i != j) {
					final boolean checkPair = validateInterventionPair(this.interventions.get(i), this.interventions.get(j));
					if (checkPair == false) {
						return false;
					}
				}
			}
		}
		return true;
	}

	// Takes a list of two interventions and will check their static intervention lists to ensure
	// there are no duplicate time + appliedTo pairs in static interventions
	private Boolean validateInterventionPair(Intervention interOne, Intervention interTwo) {
		List<StaticIntervention> staticOne = interOne.getStaticInterventions();
		List<StaticIntervention> staticTwo = interTwo.getStaticInterventions();
		for (int i = 0; i < staticOne.size(); i++) {
			for (int j = 0; j < staticTwo.size(); j++) {
				final String appliedToOne = staticOne.get(i).getAppliedTo();
				final Number timeOne = staticOne.get(i).getTimestep();
				final String appliedToTwo = staticTwo.get(j).getAppliedTo();
				final Number timeTwo = staticTwo.get(j).getTimestep();
				if (i != j && appliedToOne == appliedToTwo && timeOne == timeTwo) {
					log.warn(
						String.format(
							"The intervention % and % have duplicate applied to: % and time: % pairs.",
							interOne.getName(),
							interTwo.getName(),
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
