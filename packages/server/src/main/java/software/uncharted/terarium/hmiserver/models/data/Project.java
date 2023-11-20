package software.uncharted.terarium.hmiserver.models.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

@Data
@Accessors(chain = true)
@TSModel
@Entity
public class Project implements Serializable {
	@Serial
	private static final long serialVersionUID = 1321579058167591071L;

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@JsonProperty("id")
	private UUID id;

	private String name;

	@TSOptional
	private String description;

	@CreationTimestamp
	@JsonProperty("created_on")
	private Timestamp createdOn;

	@UpdateTimestamp
	@JsonProperty("updated_on")
	private Timestamp updatedOn;

	@JsonProperty("deleted_on")
	@TSOptional
	private Timestamp deletedOn;
}
