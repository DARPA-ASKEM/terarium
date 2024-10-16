package software.uncharted.terarium.hmiserver.models.simulationservice.interventions;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

	/**
	 * Check each intervention this policy contains
	 * Within all static interventions
	 *   if there are duplicate keys appliedTo and timestep this will throw an error
	 *   If any timestep is negative this will throw an error
	 * */
	public Boolean validateInterventionPolicy() throws Exception {
		final Set<String> foo = new HashSet<>();
		for (int i = 0; i < this.interventions.size(); i++) {
			final Intervention intervention = this.interventions.get(i);
			// For each static intervention within the policy:
			for (int j = 0; j < intervention.getStaticInterventions().size(); j++) {
				final StaticIntervention staticIntervention = intervention.getStaticInterventions().get(j);
				// Check for negative timestamps:
				final Number time = staticIntervention.getTimestep();
				if (time.doubleValue() < 0) {
					final String errorMessage = String.format(
						"The intervention %s has a timestep %s which is less than 0.",
						this.getName(),
						time.toString()
					);
					throw new Exception(errorMessage);
				}
				// Check for duplicate appliedTo timestep pairs:
				final String key = staticIntervention.getAppliedTo() + staticIntervention.getTimestep().toString();
				if (foo.contains(key)) {
					final String errorMessage = String.format(
						"The intervention %s has duplicate applied to: %s and time: %s pairs.",
						intervention.getName(),
						staticIntervention.getAppliedTo(),
						staticIntervention.getTimestep().toString()
					);
					throw new Exception(errorMessage);
				} else {
					foo.add(key);
				}
			}
		}
		return true;
	}
}
