package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ModelParameter {
	private String id;

	private String description;

	private Double value;

	private ModelGrounding grounding;

	private ModelDistribution distribution;
}

