package software.uncharted.terarium.hmiserver.models.dataservice.model.configurations;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors
@TSModel
public abstract class Semantic {
	private String name;
	private String description;
	private String source;
	private SemanticType type;
}
