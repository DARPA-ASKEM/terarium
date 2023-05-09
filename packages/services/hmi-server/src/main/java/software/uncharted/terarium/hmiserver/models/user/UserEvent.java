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
	private User user;
	private UUID id;
	private String message;

	@Override
	public String toString() {
		return "{type='" + type + "', id='" + id + "', message=" + message + "}";
	}
}
