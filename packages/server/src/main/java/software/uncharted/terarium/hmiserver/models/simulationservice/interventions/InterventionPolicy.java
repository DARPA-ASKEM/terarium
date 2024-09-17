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
import org.hibernate.annotations.Type;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;

/** Used to specify any interventions provided by the AMR and given to the simulation-service. */
@EqualsAndHashCode(callSuper = true)
@Data
@TSModel
@Accessors(chain = true)
@Entity
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
}
