package software.uncharted.terarium.hmiserver.models.simulationservice.parts;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
public class EnsembleMappingRow {

	private String name;

	@JsonAlias("dataset_mapping")
	private String datasetMapping;

	@JsonAlias("model_configuration_mappings")
	private Map<String, String> modelConfigurationMappings;
}
