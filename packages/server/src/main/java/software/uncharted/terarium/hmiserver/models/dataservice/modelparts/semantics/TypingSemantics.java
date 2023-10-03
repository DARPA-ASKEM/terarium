package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
@TSModel
public class TypingSemantics implements Serializable {
	private List<List<String>> map;
	private Object system;
}
