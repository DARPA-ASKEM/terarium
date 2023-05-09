package software.uncharted.terarium.hmiserver.models.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.oracle.svm.core.annotate.Inject;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkus.security.identity.SecurityIdentity;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.EventType;

import java.util.UUID;

@RegisterForReflection
@Data
@Accessors(chain = true)
public class UserEvent {

	@Inject
	SecurityIdentity securityIdentity;

	private EventType type;
	private User user = new User(securityIdentity);
	private UUID id;
	private JsonNode message;

	@Override
	public String toString() {
		return "{type='" + type + "', id='" + id + "', message=" + message.toString() + "}";
	}

	/**
	 * Test if passed user is the one from the event
	 * @param identity
	 * @return boolean
	 */
	public boolean isCurrentUser(final SecurityIdentity identity) {
		return this.user.equals(new User(identity));
	}
}
