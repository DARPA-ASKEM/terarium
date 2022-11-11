package software.uncharted.terarium.hmiserver.models;

import javax.json.bind.annotation.JsonbProperty;
import java.time.Instant;


public class Project {

	@JsonbProperty("id")
	public Long id;

	@JsonbProperty("name")
	public String name;

	@JsonbProperty("description")
	public String description;

	@JsonbProperty("created_at")
	public Instant createdAt;

	@JsonbProperty("updated_at")
	public Instant updatedAt;
}
