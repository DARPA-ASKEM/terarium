package software.uncharted.terarium.hmiserver.services;

import javax.enterprise.context.ApplicationScoped;

import com.oracle.svm.core.annotate.Inject;

import io.quarkus.security.identity.SecurityIdentity;
import software.uncharted.terarium.hmiserver.models.User;

@ApplicationScoped
public class UserEventService {
	@Inject
	SecurityIdentity securityIdentity;

	public User getUser() {
		return new User(securityIdentity);
	}
}
