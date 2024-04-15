package software.uncharted.terarium.hmiserver.models.notification;

import java.io.Serial;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class NotificationEvent<T> {

	@Serial
	private static final long serialVersionUID = -3382397588627700379L;

	@Id
	private UUID id = UUID.randomUUID();
	private Double progress = 0.0;
	private ProgressState state = null;

	@ManyToOne
	@JoinColumn(name = "notification_group_id", nullable = false)
	private NotificationGroup notificationGroup;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	@NotNull
	private Timestamp timestamp;

	@PrePersist
	protected void onCreate() {
		this.timestamp = this.timestamp != null ? this.timestamp
				: Timestamp.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant());
	}

	@Column
	private String data;

	public T getData() {
		try {
			return new ObjectMapper().readValue(data, new TypeReference<T>() {
			});
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public NotificationEvent<T> setData(final T data) {
		try {
			this.data = new ObjectMapper().writeValueAsString(data);
			return this;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

}
