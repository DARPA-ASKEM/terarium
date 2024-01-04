package software.uncharted.terarium.hmiserver.models.dataservice.regnet;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelGrounding;

import java.util.List;

@Data
@Accessors(chain = true)
@TSModel
public class RegNetVertex {
	private String id;
	private String name;
	private Boolean sign;

	@TSOptional
	private Object initial;

	@TSOptional
	private Object rate_constant;

	@TSOptional
	private ModelGrounding grounding;
}
