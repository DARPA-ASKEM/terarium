package software.uncharted.terarium.hmiserver.models.data.workflow;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Data
@Accessors(chain = true)
@Entity
public class WorkflowEdgePoints implements Serializable {

	@Serial
	private static final long serialVersionUID = -8873785210805680598L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false, nullable = false)
	private Long id;

	@ManyToOne
	@JoinColumn(name="workflow_edge_id", nullable=false)
	@JsonBackReference
	@NotNull
	private WorkflowEdge edge;

	private float x;

	private float y;
}
