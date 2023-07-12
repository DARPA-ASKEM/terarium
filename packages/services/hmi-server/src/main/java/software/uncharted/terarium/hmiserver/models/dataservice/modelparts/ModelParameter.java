package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

@Data
@Accessors(chain = true)
public class ModelParameter {
	private String id;

	@TSOptional
	private String name;

	@TSOptional
	private String description;

	@TSOptional
	private Double value;

	@TSOptional
	private ModelGrounding grounding;

	@TSOptional
	private ModelDistribution distribution;

	@TSOptional
	private ModelUnit unit;
}

