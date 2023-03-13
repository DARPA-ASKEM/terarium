package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ModelOperationCopy {
	private Long left;
	private String name;
	private String description;
	private String framework;
	private String content;
}
