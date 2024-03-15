package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics.OdeSemantics;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AMRSchemaType
@Accessors(chain = true)
public class ModelSemantics extends SupportAdditionalProperties implements Serializable {
	@Serial
	private static final long serialVersionUID = -3980275395523359973L;

	private OdeSemantics ode;

	@TSOptional
	private List<JsonNode> span;

	@TSOptional
	private JsonNode typing;
}
