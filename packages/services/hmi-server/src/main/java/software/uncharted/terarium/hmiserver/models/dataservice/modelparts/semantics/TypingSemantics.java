package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class TypingSemantics implements Serializable {
	private List<List<String>> map;
	private TypeSystem system;
}
