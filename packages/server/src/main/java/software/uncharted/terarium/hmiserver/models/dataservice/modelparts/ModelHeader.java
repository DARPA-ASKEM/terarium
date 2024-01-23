package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@Accessors(chain = true)
public class ModelHeader extends SupportAdditionalProperties {
	private String name;

	private String modelSchema;

	@TSOptional
	private String schemaName;

	private String description;

	@TSOptional
	private String modelVersion;

	@TSOptional
	private String extractedFrom;
}
