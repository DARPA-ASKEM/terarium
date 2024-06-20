package software.uncharted.terarium.hmiserver.models.simulationservice.parts;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;

import java.util.UUID;

/** Used to specify any interventions provided by the AMR and given to the simulation-service. */
@EqualsAndHashCode(callSuper = true)
@Data
@TSModel
@Accessors(chain = true)
@Entity
public class Intervention extends TerariumAsset {
	private Integer timestep;
	private Double value;

	private UUID modelId;

	@Override
	public String toString() {
		return " { name: " + this.getName() + " timestep: " + timestep.toString() + " value: " + value.toString() + " } ";
	}

	@Override
	public Intervention clone() {
		final Intervention clone = new Intervention();
		super.cloneSuperFields(clone);
		clone.setModelId(this.modelId);
		clone.setTimestep(this.timestep);
		clone.setValue(this.value);
		return clone;
	}
}
