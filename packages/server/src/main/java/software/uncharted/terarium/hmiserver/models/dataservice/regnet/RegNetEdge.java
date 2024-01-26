package software.uncharted.terarium.hmiserver.models.dataservice.regnet;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@Accessors(chain = true)
@TSModel
public class RegNetEdge implements SupportAdditionalProperties {
	private String source;
	private String target;
	private String id;
	private Boolean sign;

	@TSOptional
	private RegNetBaseProperties properties;
}
