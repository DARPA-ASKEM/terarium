package software.uncharted.terarium.hmiserver.models.user;

// import io.quarkus.security.identity.SecurityIdentity;

import java.util.Set;
import lombok.Data;

@Data
public class UserOld {

	private String username;
	private Set<String> roles;
	/*public User(final SecurityIdentity identity) {
		this.username = identity.getPrincipal().getName();
		this.roles = identity.getRoles();
	}*/
}
