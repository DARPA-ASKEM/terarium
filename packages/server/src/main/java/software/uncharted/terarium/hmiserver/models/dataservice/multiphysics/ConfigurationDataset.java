package software.uncharted.terarium.hmiserver.models.dataservice.multiphysics;

import java.io.Serializable;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
public class ConfigurationDataset implements Serializable {
	private String _type;
	private String type;
	private String name;
	private String description;

	private ConfigurationDatasetFile file;
}
