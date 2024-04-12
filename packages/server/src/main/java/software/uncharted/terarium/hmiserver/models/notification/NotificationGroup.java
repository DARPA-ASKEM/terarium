package software.uncharted.terarium.hmiserver.models.notification;

import java.io.Serial;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
@Entity
public class NotificationGroup {

	@Serial
	private static final long serialVersionUID = -3382397588627700379L;

	@NotNull
	private UUID id;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Timestamp createdOn;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Timestamp updatedOn;

	@OneToMany(mappedBy = "notification_event")
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@ToString.Exclude
	@JsonManagedReference
	private List<NotificationEvent> events;

	@PrePersist
	protected void onCreate() {
		this.createdOn = Timestamp.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant());
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedOn = Timestamp.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant());
	}
}
