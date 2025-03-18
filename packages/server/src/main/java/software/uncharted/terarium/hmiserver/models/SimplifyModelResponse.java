package software.uncharted.terarium.hmiserver.models;

import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;

@Data
@TSModel
public abstract class SimplifyModelResponse {

	private Model amr;
	private int max_controller_decrease;
}
