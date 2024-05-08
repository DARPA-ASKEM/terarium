package software.uncharted.terarium.hmiserver.models;

import java.io.Serializable;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Builder
@Value
@TSModel
public class ClientEvent<T> implements Serializable {
	@Builder.Default
	private UUID id = UUID.randomUUID();

	@Builder.Default
	private long createdAtMs = System.currentTimeMillis();

	private ClientEventType type;

	private T data;
}
