package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

@Data
@Accessors(chain = true)
public class ModelHeader {
	private String name;

	private String schema;

	@TSOptional
	private String schema_name;

	private String description;

	@TSOptional
	private String model_version;
}
