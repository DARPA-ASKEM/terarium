package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class VariableStatementMetadata {
	private String type;
	private String value;
}

