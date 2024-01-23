package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@Accessors(chain = true)
@TSModel
public class TypingSemantics extends SupportAdditionalProperties implements Serializable {
	private List<List<String>> map;
	private Object system;
}
