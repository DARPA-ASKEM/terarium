package software.uncharted.terarium.hmiserver.models.funman.parts;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

@Data
@Accessors(chain = true)
@TSModel
public class FunmanInterval {

	/** Upper bound * */
	@TSOptional
	private Double ub;

	/** Lower Bound * */
	@TSOptional
	private Double lb;

	@TSOptional
	private Boolean closed_upper_bound;
}
