package software.uncharted.terarium.hmiserver.models.simulationservice.parts;

import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
// Used to specify any interventions provided by the AMR and given to the simulation-service.
public class Intervention {

	private UUID id = UUID.randomUUID();
	private String name;
	private Integer timestep;
	private Double value;

	@Override
	public String toString() {
		return " { name: " + this.name + " timestep: " + timestep.toString() + " value: " + value.toString() + " } ";
	}
}
