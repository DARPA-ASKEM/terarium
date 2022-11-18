package software.uncharted.terarium.hmiserver.models.dataservice;

import javax.json.bind.annotation.JsonbProperty;
import java.time.Instant;
import java.util.List;
import java.util.Map;


public class Project {

	@JsonbProperty("id")
	public String id = null;

	@JsonbProperty("name")
	public String name;

	@JsonbProperty("description")
	public String description;

	@JsonbProperty("timestamp")
	public Instant timestamp = Instant.now();

	@JsonbProperty("status")
	public String status;

	@JsonbProperty("concept")
	public Concept concept;

	@JsonbProperty("assets")
	public Map<String, List<Long>> assets;

	public Project(final String name, final String description, final String status, final Concept concept, final Map<String, List<Long>> assets) {
		this.name = name;
		this.description = description;
		this.status = status;
		this.concept = concept;
		this.assets = assets;
	}
}
