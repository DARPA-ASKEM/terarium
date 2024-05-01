package software.uncharted.terarium.hmiserver.models.dataservice.simulation;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.dataservice.JsonConverter;

@Data
@TSModel
@Entity
public class SimulationUpdate {

	@Id
	@NotNull private UUID id = UUID.randomUUID();

	@NotNull private UUID simulationId;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	@NotNull private Timestamp createdOn;

	@PrePersist
	protected void onCreate() {
		this.createdOn = this.createdOn != null
				? this.createdOn
				: Timestamp.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant());
	}

	@Convert(converter = JsonConverter.class)
	@Column(columnDefinition = "text")
	private JsonNode data;
}
