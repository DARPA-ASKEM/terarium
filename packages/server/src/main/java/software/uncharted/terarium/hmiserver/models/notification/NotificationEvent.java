package software.uncharted.terarium.hmiserver.models.notification;

import java.io.Serial;
import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.BaseEntity;
import software.uncharted.terarium.hmiserver.models.dataservice.JsonConverter;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.ProgressState;

@Data
@Accessors(chain = true)
@TSModel
@Entity
public class NotificationEvent extends BaseEntity {

	@Serial
	private static final long serialVersionUID = -3382397588627700379L;

	private Double progress = 0.0;
	private ProgressState state = null;

	@ManyToOne
	@JoinColumn(name = "notification_group_id")
	@JsonBackReference
	@NotNull
	private NotificationGroup notificationGroup;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Timestamp acknowledgedOn = null;

	@Convert(converter = JsonConverter.class)
	@Column(columnDefinition = "text")
	private JsonNode data;

	public <T> NotificationEvent setData(final T arg) {
		data = new ObjectMapper().valueToTree(arg);
		return this;
	}
}
