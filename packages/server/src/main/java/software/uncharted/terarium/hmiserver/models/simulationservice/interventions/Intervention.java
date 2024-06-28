package software.uncharted.terarium.hmiserver.models.simulationservice.interventions;

import java.util.List;
import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@TSModel
public class Intervention {

	// TODO Types

	private String name;
	private String appliedTo;
	private List<StaticIntervention> staticInterventions;
	private List<DynamicIntervention> dynamicInterventions;
}
