package software.uncharted.terarium.hmiserver.models.user;

import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import software.uncharted.terarium.hmiserver.models.EventType;

import java.util.UUID;

@RegisterForReflection
@Data
public class UserEvent {
	private EventType type;
	private User user;
	private UUID id;
	private JsonNode message;

	@Override
	public String toString() {
		return type + "{" + "id='" + id + "', message=" + message.toString() + "}";
	}
}
