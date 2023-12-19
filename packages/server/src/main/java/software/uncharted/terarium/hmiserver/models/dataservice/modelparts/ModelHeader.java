package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

@Data
@Accessors(chain = true)
public class ModelHeader {
	private String name;

	@JsonAlias("model_schema")
	private String modelSchema;

	@TSOptional
	@JsonAlias("schema_name")
	private String schemaName;

	private String description;

	@TSOptional
	@JsonAlias("model_version")
	private String modelVersion;

	@TSOptional
	@JsonAlias("extracted_from")
	private String extractedFrom;
}
