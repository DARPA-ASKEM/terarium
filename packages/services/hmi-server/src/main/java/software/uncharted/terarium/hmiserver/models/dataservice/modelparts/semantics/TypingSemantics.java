package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics;

import lombok.Data;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@Data
@Accessors(chain = true)
public class TypingSemantics {

	private TypeSystem type_system;

	private List<List<String>> type_map;
}
