package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

import java.io.Serial;
import java.io.Serializable;

@Data
@AMRSchemaType
@Accessors(chain = true)
public class ModelUnit extends SupportAdditionalProperties implements Serializable {
	@Serial
	private static final long serialVersionUID = 2545917939916234517L;

	private String expression;

	@JsonProperty("expression_mathml")
	private String expressionMathml;
}
