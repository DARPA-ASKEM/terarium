package software.uncharted.terarium.hmiserver.models.dataservice.regnet;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelGrounding;

import java.util.List;

@Data
@Accessors(chain = true)
@TSModel
public class RegNetBaseProperties {
	private String name;
	private ModelGrounding grounding;
	private Object rate_constant;
}
