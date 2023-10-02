package software.uncharted.terarium.hmiserver.models.user;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.EventType;

import java.util.UUID;

//@RegisterForReflection
@TSModel
@Data
public class UserEvent {
	private EventType type;
	private UserOld user;
	private UUID id;
	private JsonNode message;

	@Override
	public String toString() {
		return type + "{" + "id='" + id + "', message=" + message.toString() + "}";
	}
}
