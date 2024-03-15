package software.uncharted.terarium.hmiserver.models.dataservice.regnet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelGrounding;

import java.io.Serial;
import java.io.Serializable;

@Data
@Accessors(chain = true)
@TSModel
public class RegNetBaseProperties extends SupportAdditionalProperties implements Serializable {
	@Serial
	private static final long serialVersionUID = 762993961915920424L;

	private String name;

	private ModelGrounding grounding;

	@JsonProperty("rate_constant")
	private Object rateConstant;
}
