package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelGrounding;

import java.io.Serial;
import java.io.Serializable;

@Data
@AMRSchemaType
@Accessors(chain = true)
public class Initial extends SupportAdditionalProperties implements Serializable {
	@Serial
	private static final long serialVersionUID = 492480233470286509L;

	@TSOptional
	private String description;

	@TSOptional
	private ModelGrounding grounding;

	private String target;

	private String expression;

	@JsonProperty("expression_mathml")
	private String expressionMathml;
}
