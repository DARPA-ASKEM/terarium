package software.uncharted.terarium.hmiserver.models.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
public class ProjectAsset implements Serializable {
	@Serial
	private static final long serialVersionUID = -4647375493078889464L;

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@NotNull
	private UUID projectId;

	@NotNull
	private String resourceId;

	@NotNull
	@Enumerated(EnumType.STRING)
	private ResourceType resourceType;

	@TSOptional
	private String externalRef;

	@CreationTimestamp
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Timestamp createdOn;

	@UpdateTimestamp
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Timestamp updatedOn;

	@TSOptional
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Timestamp deletedOn;
}
