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

	public UserEvent create(User user) {
		return new UserEvent()
			.setUser(user)
			.setId(UUID.randomUUID());
	}

	public void send(EventType type, String message, User user) {
		final UserEvent event = create(user)
			.setType(type)
			.setMessage(message);
		userEventEmitter.send(event);
	}

	/**
	 * If no User is defined, we use the current user
	 * @param type
	 * @param message
	 */
	public void send(EventType type, String message) {
		final User user = new User(securityIdentity);
		send(type, message, user);
	}

	public boolean isCurrentUser(UserEvent event) {
		return event.getUser().equals(new User(securityIdentity));
	}
}
