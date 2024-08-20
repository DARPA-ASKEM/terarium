package software.uncharted.terarium.hmiserver.models.dataservice.simulation;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;
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
