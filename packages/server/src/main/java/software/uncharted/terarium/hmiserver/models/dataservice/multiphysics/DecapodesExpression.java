package software.uncharted.terarium.hmiserver.models.dataservice.multiphysics;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

/**
 * See
 * https://algebraicjulia.github.io/SyntacticModels.jl/dev/generated/composite_models_examples/#Specifying-the-Component-Systems
 * https://algebraicjulia.github.io/SyntacticModels.jl/dev/generated/decapodes_examples/
 */
@Data
@Accessors(chain = true)
@TSModel
public class DecapodesExpression {

	private List<Object> context;
	private List<DecapodesEquation> equations;
	private String _type;
}
