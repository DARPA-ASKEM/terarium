package software.uncharted.terarium.hmiserver.models.funman;

import java.io.Serializable;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.funman.parts.FunmanWorkRequest;

@Data
@Accessors(chain = true)
@TSModel
public class FunmanPostQueriesRequest implements Serializable {

	private Model model;
	private FunmanWorkRequest request;
}
