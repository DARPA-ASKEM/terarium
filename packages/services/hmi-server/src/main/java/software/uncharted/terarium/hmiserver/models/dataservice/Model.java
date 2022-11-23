package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

@Data
@Accessors(chain = true)
class ModelContent implements Serializable {
	public Map<String, String>[] S;

	public Map<String, Optional<String>>[] T;

	public Map<String, Number>[] I;

	public Map<String, Number>[] O;
}
