package software.uncharted.terarium.hmiserver.models.notification;

import java.io.Serial;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.ProgressState;

@Data
@Accessors(chain = true)
@TSModel
@Entity
public class NotificationEvent {

	@Serial
	private static final long serialVersionUID = -3382397588627700379L;

	@NotNull
	private UUID id;

	private String message;
	private Double progress;
	private ProgressState state;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	@NotNull
	private Timestamp timestamp;

	@PrePersist
	protected void onCreate() {
		this.timestamp = Timestamp.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant());
	}

}
