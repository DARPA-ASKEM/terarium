package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

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
	@JsonProperty("odeSemantics") // camel case in schema
	private OdeSemantics odeSemantics;

	@TSOptional
	private List<Object> span;

	@TSOptional
	@JsonProperty("typingSemantics") // camel case in schema
	private TypingSemantics typingSemantics;
}
