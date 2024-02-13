package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

import java.io.Serial;
import java.io.Serializable;

@Data
@AMRSchemaType
@Accessors(chain = true)
public class ModelParameter extends SupportAdditionalProperties implements Serializable {
	@Serial
	private static final long serialVersionUID = -8680842000646488249L;

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
