package software.uncharted.terarium.hmiserver.models.funman.responses;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.funman.requests.FunmanRequest;

@Data
@Accessors(chain = true)
@TSModel
public class FunmanPostQueriesResponse {
	private String id;
	private Model model;  // Assuming the correct import statement for Model class
	private FunmanRequest request;
}
