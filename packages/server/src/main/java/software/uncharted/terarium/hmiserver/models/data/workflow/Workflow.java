package software.uncharted.terarium.hmiserver.models.data.workflow;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * The workflow data structure is not very well defined. It is also meant to carry operations each with their own unique
 * representations. As such this is just a pass-thru class for the proxy. The UI has it's own typinging definition that is
 * not generated.
 */
@Data
@Accessors(chain = true)
@Entity
public class Workflow implements Serializable {

	@Serial
	private static final long serialVersionUID = 8130209563056329205L;

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private UUID id;

	private String name;

	private String description;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private WorkflowTransform transform;

	@OneToMany(mappedBy = "workflow")
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@ToString.Exclude
	@JsonManagedReference
	private List<WorkflowNode> nodes;

	@OneToMany(mappedBy = "workflow")
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@ToString.Exclude
	@JsonManagedReference
	private List<WorkflowEdge> edges;

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
