package software.uncharted.terarium.hmiserver.models.user;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.EventType;

@Entity
@Data
@TSModel
@Accessors(chain = true)
@NoArgsConstructor
@Table(
	indexes = {
		@Index(columnList = "timestampmillis"),
		@Index(columnList = "projectid"),
		@Index(columnList = "userid"),
		@Index(columnList = "type"),
		@Index(columnList = "value")
	}
)
public class Event implements Serializable {

	@Serial
	private static final long serialVersionUID = 7337142167492880031L;

	@Id
	@TSOptional
	private String id = UUID.randomUUID().toString();

	@Column(nullable = false)
	@TSOptional
	private Long timestampMillis = Instant.now().toEpochMilli();

	@TSOptional
	private UUID projectId;

	@Column(nullable = false)
	@TSOptional
	private String userId;

	@Column(nullable = false)
	private EventType type;

	@Column(columnDefinition = "TEXT")
	@TSOptional
	private String value;
}
