package software.uncharted.terarium.hmiserver.models.dataservice.simulation;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.dataservice.TerariumAsset;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TSModel
@Entity
public class SimulationResult extends TerariumAsset implements Serializable {

	@Serial
	private static final long serialVersionUID = 4211271157196613944L;


	@ManyToOne
	@JoinColumn(name = "simulation_id", nullable = false)
	@JsonBackReference
	@NotNull
	private Simulation simulation;

	private String filename;

}
