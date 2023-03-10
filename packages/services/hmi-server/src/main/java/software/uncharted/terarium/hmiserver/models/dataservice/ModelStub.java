package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


/**
 * This is used to mirror model-creation and model-update
**/
@Data
@Accessors(chain = true)
public class ModelStub implements Serializable {
	private String name;
	private String description;
	private String framework;
	private String content;
}

