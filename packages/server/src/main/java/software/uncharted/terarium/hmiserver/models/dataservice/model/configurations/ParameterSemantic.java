package software.uncharted.terarium.hmiserver.models.dataservice.model.configurations;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelDistribution;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelUnit;
import software.uncharted.terarium.hmiserver.models.simulationservice.parts.Intervention;

@EqualsAndHashCode(callSuper = true)
@Data
@TSModel
@Accessors
public class ParameterSemantic extends Semantic {
	private String id;
	private Number value;
	private ModelDistribution distribution;
	private boolean isDefault;
	private Intervention[] interventions;
	private ModelUnit modelUnit;
}
