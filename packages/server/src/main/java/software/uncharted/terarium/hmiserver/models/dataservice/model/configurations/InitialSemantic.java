package software.uncharted.terarium.hmiserver.models.dataservice.model.configurations;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors
@TSModel
public class InitialSemantic extends Semantic {
	private String target;
	private String expression;
	private String unit;
}
