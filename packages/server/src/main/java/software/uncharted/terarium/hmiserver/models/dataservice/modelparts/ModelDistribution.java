package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
@AMRSchemaType
@Accessors(chain = true)
public class ModelDistribution extends SupportAdditionalProperties implements Serializable {
	@Serial
	private static final long serialVersionUID = -5426742497090710018L;

	private String type;

	private Map<String, Object> parameters;
}
