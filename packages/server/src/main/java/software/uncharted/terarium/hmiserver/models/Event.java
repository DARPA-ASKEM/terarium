package software.uncharted.terarium.hmiserver.models;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
@Accessors(chain = true)
@TSModel
public class Event implements Serializable {

	@TSOptional
	private String id = UUID.randomUUID().toString();

	@TSOptional
	private Long timestampMillis = Instant.now().toEpochMilli();

	@TSOptional
	private Long projectId;

	@TSOptional
	private String username;

	private EventType type;

	@TSOptional
	private String value;
}
