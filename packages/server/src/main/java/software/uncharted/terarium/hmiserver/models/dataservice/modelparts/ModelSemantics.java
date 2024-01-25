package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics.OdeSemantics;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics.TypingSemantics;

@Data
@AMRSchemaType
@Accessors(chain = true)
public class ModelSemantics implements SupportAdditionalProperties {
	private OdeSemantics ode;

	@TSOptional
	private List<Object> span;

	@TSOptional
	private TypingSemantics typing;
}
