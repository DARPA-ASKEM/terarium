package software.uncharted.terarium.hmiserver.models.dataservice.multiphysics;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import java.io.Serializable;

@Data
@Accessors(chain = true)
@TSModel
public class ConfigurationParameter implements Serializable {
	private String _type;
	private String type;
	private Object value;
}
