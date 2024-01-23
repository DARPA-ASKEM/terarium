package software.uncharted.terarium.hmiserver.models.dataservice.model;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@Accessors(chain = true)
public class ModelOperationCopy extends SupportAdditionalProperties {
	private Long left;
	private String name;
	private String description;
	private String framework;
	private String content;
}
