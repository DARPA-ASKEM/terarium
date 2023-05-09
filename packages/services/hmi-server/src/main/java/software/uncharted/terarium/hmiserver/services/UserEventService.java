package software.uncharted.terarium.hmiserver.services;

import io.quarkus.security.identity.SecurityIdentity;
import software.uncharted.terarium.hmiserver.models.user.User;
import software.uncharted.terarium.hmiserver.models.user.UserEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class UserEventService {
	@Inject
	SecurityIdentity securityIdentity;

	public UserEvent generateUserEvent() {
		return new UserEvent()
			.setUser(new User(securityIdentity))
			.setId(UUID.randomUUID());
	}
}
