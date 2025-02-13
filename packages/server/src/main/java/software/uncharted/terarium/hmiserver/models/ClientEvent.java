package software.uncharted.terarium.hmiserver.models;

import java.io.Serializable;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

@Builder
@Value
@TSModel
public class ClientEvent<T> implements Serializable {

	@Builder.Default
	private UUID id = UUID.randomUUID();

	@Builder.Default
	private long createdAtMs = System.currentTimeMillis();

	private ClientEventType type;

	@TSOptional
	private UUID projectId;

	@TSOptional
	private UUID notificationGroupId;

	@TSOptional
	private String userId; // Event created by or due to userId

	private T data;
}
