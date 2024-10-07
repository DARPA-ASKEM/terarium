package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@EqualsAndHashCode(callSuper = true)
@AMRSchemaType
@Accessors(chain = true)
public class ModelHeader extends SupportAdditionalProperties implements Serializable {

	@Serial
	private static final long serialVersionUID = 8627939844152271422L;

	private String name;

	@JsonProperty("schema")
	@JsonAlias("schema_") // Funman returns schema_ instead of schema
	private String modelSchema;

	@TSOptional
	@JsonProperty("schema_name")
	private String schemaName;

	private String description;

	@TSOptional
	@JsonProperty("model_version")
	private String modelVersion;

	@TSOptional
	@JsonProperty("extracted_from")
	private String extractedFrom;

	@Override
	public ModelHeader clone() {
		ModelHeader clone = (ModelHeader) super.clone();
		clone.setName(this.getName());
		clone.setModelSchema(this.getModelSchema());
		clone.setSchemaName(this.getSchemaName());
		clone.setDescription(this.getDescription());
		clone.setExtractedFrom(this.getExtractedFrom());
		clone.setModelVersion(this.getModelVersion());

		return clone;
	}
}
