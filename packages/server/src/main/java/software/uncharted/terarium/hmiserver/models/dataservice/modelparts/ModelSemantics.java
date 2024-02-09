package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics.OdeSemantics;

@Data
@AMRSchemaType
@Accessors(chain = true)
public class ModelSemantics implements SupportAdditionalProperties {
	private OdeSemantics ode;

	@TSOptional
	private List<JsonNode> span;

	@TSOptional
	private JsonNode typing;
}
