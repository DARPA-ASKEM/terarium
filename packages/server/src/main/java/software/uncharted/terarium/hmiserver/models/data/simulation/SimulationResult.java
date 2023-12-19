 package software.uncharted.terarium.hmiserver.models.data.simulation;

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

 import java.io.Serial;
 import java.io.Serializable;
 import java.sql.Timestamp;
 import java.util.UUID;

@Data
@Accessors(chain = true)
@TSModel
@Entity
public class SimulationResult implements Serializable {

	@Serial
	private static final long serialVersionUID = 4211271157196613944L;
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private UUID id;

	@ManyToOne
	@JoinColumn(name="simulation_id", nullable=false)
	@JsonBackReference
	@NotNull
	private Simulation simulation;

	private String filename;

	@TSOptional
	@CreationTimestamp
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Timestamp createdOn;

	@TSOptional
	@UpdateTimestamp
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Timestamp updatedOn;

	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Timestamp deletedOn;
}
