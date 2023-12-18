package software.uncharted.terarium.hmiserver.models.data.project;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.dataservice.Assets;

@Data
@Accessors(chain = true)
@TSModel
@Entity
public class Project implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private UUID id;

	@Schema(defaultValue = "My New Project")
	private String name;

	private String userId;

	@TSOptional
	@Schema(defaultValue = "My Project Description")
	private String description;

	@CreationTimestamp
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Timestamp createdOn;

	@UpdateTimestamp
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Timestamp updatedOn;

	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Timestamp deletedOn;

	@OneToMany(mappedBy = "project")
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@ToString.Exclude
	@JsonManagedReference
	private List<ProjectAsset> projectAssets;

	@TSOptional
	@Transient
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Deprecated
	private Assets assets;

	@TSOptional
	@Transient
	@Schema(accessMode = Schema.AccessMode.READ_ONLY, defaultValue = "{}")
	// Metadata that can be useful for the UI
	private Map<String, String> metadata;

	@TSOptional
	@Transient
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private Boolean publicProject;

	@TSOptional
	@Transient
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private String userPermission;
}
