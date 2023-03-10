package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class ModelOperationCopy implements Serializable {
	private Long left;
	private String name;
	private String description;
	private String framework;
	private String content;
}
