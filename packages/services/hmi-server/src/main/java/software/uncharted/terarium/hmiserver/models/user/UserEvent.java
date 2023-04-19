package software.uncharted.terarium.hmiserver.models.user;

import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.runtime.annotations.RegisterForReflection;
import software.uncharted.terarium.hmiserver.models.EventType;

import java.util.UUID;

@RegisterForReflection
public class UserEvent<S> {
	private EventType type;
	private User user;
	private UUID id;
	private JsonNode message;

	@Override
	public String toString() {
		return type + "{" + "id='" + id + "', message=" + message.toString() + "}";
	}
}
