package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class ModelFramework implements Serializable {

	private String name;

	private String version;

	private String semantics;
}
