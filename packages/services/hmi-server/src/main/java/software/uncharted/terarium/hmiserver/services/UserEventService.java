package software.uncharted.terarium.hmiserver.services;

import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import software.uncharted.terarium.hmiserver.models.EventType;
import software.uncharted.terarium.hmiserver.models.user.User;
import software.uncharted.terarium.hmiserver.models.user.UserEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class UserEventService {
	@Inject
	SecurityIdentity securityIdentity;

	@Broadcast
	@Channel("user-event")
	Emitter<UserEvent> userEventEmitter;

	/**
	 * Create a new UserEvent for a specific user, without any content
	 * @param identity
	 * @return
	 */
	public UserEvent createEmptyEvent(SecurityIdentity identity) {
		return new UserEvent()
			.setUser(new User(identity))
			.setId(UUID.randomUUID());
	}

	/**
	 * Send a UserEvent
	 * @param type
	 * @param message
	 */
	public void send(EventType type, String message) {
		final UserEvent event = createEmptyEvent(securityIdentity)
			.setType(type)
			.setMessage(message);

		userEventEmitter.send(event);
	}

	public boolean isCurrentUser(UserEvent event) {
		return event.getUser().equals(new User(securityIdentity));
	}
}
