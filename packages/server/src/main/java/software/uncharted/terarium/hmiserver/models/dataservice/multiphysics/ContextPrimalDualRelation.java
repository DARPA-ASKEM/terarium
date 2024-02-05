package software.uncharted.terarium.hmiserver.models.dataservice.multiphysics;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import java.io.Serializable;

@Data
@Accessors(chain = true)
@TSModel
public class ContextPrimalDualRelation implements Serializable {
	private String primal;
	private String dual;
	private JsonNode method;
}
