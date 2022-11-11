package software.uncharted.terarium.hmiserver.models;

import javax.json.bind.annotation.JsonbProperty;
import java.time.Instant;


public class Model {

	@JsonbProperty("id")
	public Long id;

	@JsonbProperty("name")
	public String name;

	@JsonbProperty("description")
	public String description;

	@JsonbProperty("framework")
	public String framework;

	@JsonbProperty("timestamp")
	public Instant timestamp;

	@JsonbProperty("content")
	public String content;

}
