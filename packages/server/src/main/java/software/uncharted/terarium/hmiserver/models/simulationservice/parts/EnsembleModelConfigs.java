package software.uncharted.terarium.hmiserver.models.simulationservice.parts;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
// Used to specify the location of a dataset for simulation-service
public class EnsembleModelConfigs {

	private String id;

	@JsonAlias("solution_mappings")
	private Map<String, String> solutionMappings;

	private float weight;
}
