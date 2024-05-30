package software.uncharted.terarium.hmiserver.models.dataservice.multiphysics;

import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
public class ConfigurationDatasetFile implements Serializable {
	private String _type;
	private String uri;
	private String format;
	private List<Number> shape;
}
