package software.uncharted.terarium.hmiserver.models.dataservice.model.configurations;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics.State;

@EqualsAndHashCode(callSuper = true)
@Data
@TSModel
@Accessors
public class ObservableSemantic extends Semantic {
	private String id;
	private State[] states;
	private String expression;
}
