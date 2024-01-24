package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@AMRSchemaType
@Accessors(chain = true)
public class StatementValue implements SupportAdditionalProperties {
	private String value;
	private String type;

	@TSOptional
	@JsonProperty("dkg_grounding")
	private DKGConcept dkgGrounding;
}
