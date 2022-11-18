package software.uncharted.terarium.hmiserver.models;

import javax.json.bind.annotation.JsonbProperty;
import java.util.Map;


public class Model {

	@JsonbProperty("id")
	public Long id;

	@JsonbProperty("name")
	public String name;

	@JsonbProperty("description")
	public String description;

	@JsonbProperty("content")
	public String content;

	@JsonbProperty("parameters")
	public Map<String, String> parameters;
}
