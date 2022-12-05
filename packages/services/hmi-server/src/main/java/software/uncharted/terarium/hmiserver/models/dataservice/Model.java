package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Data
@Accessors(chain = true)
public class Model implements Serializable {

	private String id;

	private String name;

	private String description;

	private ModelFramework framework;

	private Instant timestamp;

	private ModelContent content;

	private Concept concept;

	private Map<String, String> parameters = new HashMap<>();
}

