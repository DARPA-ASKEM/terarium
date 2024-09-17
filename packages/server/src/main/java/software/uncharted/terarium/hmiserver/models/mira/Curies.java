package software.uncharted.terarium.hmiserver.models.mira;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
public class Curies {

	private List<String> sources;
	private List<String> targets;
}
