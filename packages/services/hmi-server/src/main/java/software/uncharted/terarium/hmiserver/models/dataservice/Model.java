package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Data
@Accessors(chain = true)
public class Model extends ResourceType implements Serializable {

	private String name;

	private String description;

	private String framework;

	private LocalDateTime timestamp;

	private ModelContent content;

	private Concept concept;

	private Map<String, Object> parameters = new HashMap<>();
}

