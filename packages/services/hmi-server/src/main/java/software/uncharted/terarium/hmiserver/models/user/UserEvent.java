package software.uncharted.terarium.hmiserver.models.user;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.EventType;

import java.util.UUID;

@RegisterForReflection
@Data
@Accessors(chain = true)
public class UserEvent {
	private EventType type;
	private User user;
	private UUID id;
	private String message;

	@Override
	public String toString() {
		return "{type='" + type + "', id='" + id + "', message=" + message + "}";
	}
}
