package software.uncharted.terarium.hmiserver.models.dataservice.regnet;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSIgnore;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelGrounding;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics.GroundedSemantic;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors
@TSModel
public class RegNetVertex extends SupportAdditionalProperties implements Serializable, GroundedSemantic {

	@Serial
	private static final long serialVersionUID = 5172147247116021094L;

	private String id;

	private String name;

	private Boolean sign;

	@TSOptional
	private Object initial;

	@TSOptional
	@JsonProperty("rate_constant")
	private Object rateConstant;

	@TSOptional
	private ModelGrounding grounding;

	@TSIgnore
	private String description;
}
