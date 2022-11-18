package software.uncharted.terarium.hmiserver.models.dataservice;

import javax.json.bind.annotation.JsonbProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Project {

	@JsonbProperty("id")
	public String id = null;

	@JsonbProperty("name")
	public String name;

	@JsonbProperty("description")
	public String description;

	@JsonbProperty("assets")
	public Map<String, List<Long>> assets = new HashMap<>();

	@JsonbProperty("status")
	public String status;

	public Project(final String name, final String description) {
		this.name = name;
		this.description = description;
	}
}
