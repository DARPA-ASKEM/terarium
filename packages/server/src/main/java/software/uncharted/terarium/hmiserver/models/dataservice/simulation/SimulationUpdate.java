package software.uncharted.terarium.hmiserver.models.dataservice.simulation;

import java.io.Serial;
import java.util.UUID;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.JsonNode;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.TerariumEntity;

@EqualsAndHashCode(callSuper = true)
@Data
@TSModel
@Entity
public class SimulationUpdate extends TerariumEntity {

	@Serial
	private static final long serialVersionUID = -2346524355234700379L;

	@ManyToOne
	@JsonBackReference
	@NotNull
	private Simulation simulation;

	@Column(name = "sequence_number", nullable = false, unique = true)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_gen")
	@SequenceGenerator(name = "seq_gen", sequenceName = "sequence_number_seq", allocationSize = 1)
	private Integer sequenceNumber;

	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private JsonNode data;

	public SimulationUpdate clone(final Simulation simulation) {
		final SimulationUpdate clone = new SimulationUpdate();
		clone.setId(UUID.randomUUID());
		clone.setSimulation(simulation);
		clone.setCreatedOn(this.getCreatedOn());
		clone.setData(this.data);
		return clone;
	}
}
