package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ModelParameter {
	private String id;

	private String description;

	private Double value;

	// FIXME:
	private String grounding;
	private String distribution;
}

