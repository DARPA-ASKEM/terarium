package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.documentservice.Document;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@TSModel
@Entity
public class Project implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@TSOptional
	private UUID id;

	private String name;

	@TSOptional
	private String description;

	@CreationTimestamp
	private Timestamp createdOn;

	@UpdateTimestamp
	private Timestamp updatedOn;

	@TSOptional
	private Timestamp deletedOn;

	@TSOptional
	@Transient
	private Assets assets;

	@TSOptional
	@Transient
	// Metadata that can be useful for the UI
	private Map<String, String> metadata;

	@TSOptional
	@Transient
	private Boolean publicProject;

	@TSOptional
	@Transient
	private String userPermission;

	/**
	 * Helper method to create a new project from an existing one, excluding UUID and timestamps. This
	 * will be useful for creating new projects to save in the database from the client for example
	 * @param oldProject project to copy from
	 * @return new project
	 */
	public static Project cloneFrom(Project oldProject){
		return new Project()
			.setName(oldProject.getName())
			.setDescription(oldProject.getDescription())
			.setAssets(oldProject.getAssets());
	}
}
