package software.uncharted.terarium.hmiserver.models.dataservice.multiphysics;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.io.Serializable;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
public class Configuration implements Serializable {

	private Map<String, ConfigurationParameter> parameters;

	@JsonAlias("initial_conditions")
	private Map<String, ConfigurationCondition> initialConditions;

	@JsonAlias("boundary_conditions")
	private Map<String, ConfigurationCondition> boundryConditions;

	private Map<String, ConfigurationDataset> datasets;
}
