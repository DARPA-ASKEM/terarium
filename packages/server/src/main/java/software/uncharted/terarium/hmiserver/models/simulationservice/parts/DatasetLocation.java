package software.uncharted.terarium.hmiserver.models.simulationservice.parts;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

@Data
@Accessors(chain = true)
@TSModel
// Used to specify the location of a dataset for simulation-service
public class DatasetLocation {

	private String id;
	private String filename;

	@TSOptional
	private Object mappings;
}
