package software.uncharted.terarium.hmiserver.models.mira;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
public class EntitySimilarityResult {

	private String source;
	private String target;
	private Float similarity;
}
