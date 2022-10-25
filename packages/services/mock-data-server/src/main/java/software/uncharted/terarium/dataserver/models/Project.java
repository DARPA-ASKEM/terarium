package software.uncharted.terarium.mockdataserver.models;

import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import java.time.Instant;
import java.util.UUID;


@NoArgsConstructor
@Schema(description = "A TERArium project")
public class Project {

	@JsonbProperty("id")
	@Schema(required = true)
	public UUID id = UUID.randomUUID();

  @JsonbProperty("name")
	@Schema(required = true)
  public String name;

	@JsonbProperty("description")
	public String description;

	@JsonbProperty("created_at")
  public Instant createdAt = Instant.now();

	@JsonbProperty("updated_at")
  public Instant updatedAt = Instant.now();

	@JsonbTransient
	public Instant deletedAt = null;

	public Project(String name, String description) {
		this.name = name;
		this.description = description;
	}

	@Override
	public String toString() {
		return "Project{" +
			"name='" + name + '\'' +
			", description='" + description + '\'' +
			", createdAt=" + createdAt +
			", updatedAt=" + updatedAt +
			", deletedAt=" + deletedAt +
			'}';
	}
}
