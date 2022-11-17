package software.uncharted.terarium.hmiserver.models;

import javax.json.bind.annotation.JsonbProperty;
import java.util.List;
import java.util.Map;


public class Project {

	@JsonbProperty("id")
	public Long id;

	@JsonbProperty("name")
	public String name;

	@JsonbProperty("description")
	public String description;

	@JsonbProperty("assets")
	public Map<String, List<Long>> assets;

	@JsonbProperty("status")
	public String status;
}
