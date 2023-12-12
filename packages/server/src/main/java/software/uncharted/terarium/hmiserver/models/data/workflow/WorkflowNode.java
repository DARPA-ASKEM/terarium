package software.uncharted.terarium.hmiserver.models.data.workflow;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;


@Data
@Accessors(chain = true)
@Entity
public class WorkflowNode implements Serializable {

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

	private String operationType;

	private String displayName;

	private Double x;

	private Double y;

	private Object state;

	private Object inputs;

	private Object outputs;

	private String status;

	private Integer width;

	private Integer height;
}
