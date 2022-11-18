package software.uncharted.terarium.hmiserver.models.dataservice;

import javax.json.bind.annotation.JsonbProperty;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;


public class Model {

	@JsonbProperty("id")
	public String id = null;

	@JsonbProperty("name")
	public String name;

	@JsonbProperty("description")
	public String description;

	@JsonbProperty("framework")
	public ModelFramework framework;

	@JsonbProperty("timestamp")
	public Instant timestamp;

	@JsonbProperty("content")
	public String content;

	@JsonbProperty("concept")
	public Concept concept;

	@JsonbProperty("parameters")
	public Map<String, String> parameters = new HashMap<>();

	public Model(final String name, final String description, final ModelFramework framework, final Instant timestamp, final String content, final Concept concept) {
		this.name = name;
		this.description = description;
		this.framework = framework;
		this.timestamp = timestamp;
		this.content = content;
		this.concept = concept;
	}
}
