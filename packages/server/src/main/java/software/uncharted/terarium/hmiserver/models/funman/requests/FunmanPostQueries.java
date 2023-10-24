package software.uncharted.terarium.hmiserver.models.funman.requests;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;

@Data
@Accessors(chain = true)
@TSModel

public class FunmanPostQueries {
	private Model model;
	private FunmanRequest request; 
}
