package software.uncharted.terarium.hmiserver.models.data.simulation;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@TSModel
@Entity
public class Simulation implements Serializable {

	@Serial
	private static final long serialVersionUID = 5467224100686908152L;

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private UUID id;

	@JsonAlias("execution_payload")
	private Object executionPayload;

	@TSOptional
	private String name;

	@TSOptional
	private String description;

	@JsonAlias("result_files")
	@TSOptional
	@OneToMany(mappedBy = "simulation")
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@ToString.Exclude
	@JsonManagedReference
	private List<SimulationResult> resultFiles;

	@Enumerated(EnumType.STRING)
	private SimulationType type;

	@Enumerated(EnumType.STRING)
	private SimulationStatus status;

	@JsonAlias("start_time")
	@TSOptional
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Timestamp startTime;

	@JsonAlias("completed_time")
	@TSOptional
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Timestamp completedTime;

	@Enumerated(EnumType.STRING)
	private SimulationEngine engine;

	@JsonAlias("workflow_id")
	private UUID workflowId;

	@JsonAlias("user_id")
	@TSOptional
	private Integer userId;

	@JsonAlias("project_id")
	@TSOptional
	private Integer projectId;

	@CreationTimestamp
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Timestamp createdOn;

	@UpdateTimestamp
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Timestamp updatedOn;

	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Timestamp deletedOn;
}
