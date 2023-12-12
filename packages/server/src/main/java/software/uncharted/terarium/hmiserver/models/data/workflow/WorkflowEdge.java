package software.uncharted.terarium.hmiserver.models.data.workflow;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;


@Data
@Accessors(chain = true)
@Entity
public class WorkflowEdge implements Serializable {

	@Serial
	private static final long serialVersionUID = -3383148327814419415L;

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private UUID id;

	@ManyToOne
	@JoinColumn(name="workflow_id", nullable=false)
	@JsonBackReference
	@NotNull
	private Workflow workflow;

	private UUID source;

	private UUID sourcePortId;

	private UUID target;

	private UUID targetPortId;

	@OneToMany(mappedBy = "edge")
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@ToString.Exclude
	@JsonManagedReference
	private List<WorkflowEdgePoints> points;
}
