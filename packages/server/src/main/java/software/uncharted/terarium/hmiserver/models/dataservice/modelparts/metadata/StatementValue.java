package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@Accessors(chain = true)
public class StatementValue extends SupportAdditionalProperties {
	private String value;
	private String type;

	@TSOptional
	private DKGConcept dkg_grounding;
}
