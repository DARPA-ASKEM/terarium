package software.uncharted.terarium.hmiserver.models.notification;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.sql.Timestamp;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import software.uncharted.terarium.hmiserver.ProgressState;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.TerariumEntity;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TSModel
@Entity
@Table(
	name = "notification_event",
	indexes = {
		@Index(name = "idx_notification_group_id", columnList = "notification_group_id"),
		@Index(name = "idx_acknowledged_on", columnList = "acknowledgedOn")
	}
)
public class NotificationEvent extends TerariumEntity {

	@Serial
	private static final long serialVersionUID = -3382397588627700379L;

	private Double progress = 0.0;

	@Enumerated(EnumType.STRING)
	private ProgressState state = null;

	@ManyToOne
	@JsonBackReference
	@NotNull
	private NotificationGroup notificationGroup;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Timestamp acknowledgedOn = null;

	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private JsonNode data;

	public <T> NotificationEvent setData(final T arg) {
		data = new ObjectMapper().valueToTree(arg);
		return this;
	}
}
