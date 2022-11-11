package software.uncharted.terarium.mockdataservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.json.bind.annotation.JsonbProperty;
import java.time.Instant;

@Data
@AllArgsConstructor
public class Project {

	@JsonbProperty("id")
	private Long id;

	@JsonbProperty("name")
	public String name;

	@JsonbProperty("description")
	public String description;

	@JsonbProperty("created_at")
	public Instant createdAt = Instant.now();

	@JsonbProperty("updated_at")
	public Instant updatedAt = Instant.now();

	public Project(final Long id, final String name, final String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}
}
