package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import lombok.Data;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata.VariableStatement;

@Data
@Accessors(chain = true)
public class TypingSemantics {

	@JsonProperty("type_system")
	private TypeSystem typeSystem;

	@JsonProperty("type_map")
	private List<List<String>> typeMap;
}
