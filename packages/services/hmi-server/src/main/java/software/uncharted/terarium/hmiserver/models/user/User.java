package software.uncharted.terarium.hmiserver.models.user;

import io.quarkus.security.identity.SecurityIdentity;
import lombok.Data;

import java.util.Set;

@Data
public class User {
	private String username;
	private Set<String> roles;

	public User(final SecurityIdentity identity) {
		this.username = identity.getPrincipal().getName();
		this.roles = identity.getRoles();
	}
}
