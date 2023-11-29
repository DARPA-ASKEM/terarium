package software.uncharted.terarium.hmiserver.models.data.project;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

@Data
@Accessors(chain = true)
@TSModel
@Entity
public class ProjectAsset implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private UUID id;

	@ManyToOne
	@JoinColumn(name="project_id", nullable=false)
	@JsonBackReference
	@NotNull
	private Project project;

	@NotNull
	private String resourceId;

	@NotNull
	@Enumerated(EnumType.STRING)
	private ResourceType resourceType;

	@TSOptional
	private String externalRef;

	@CreationTimestamp
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private Timestamp createdOn;

	@UpdateTimestamp
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private Timestamp updatedOn;

	@TSOptional
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private Timestamp deletedOn;
}
