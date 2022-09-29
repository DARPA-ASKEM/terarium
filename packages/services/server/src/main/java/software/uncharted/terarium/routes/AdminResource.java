package software.uncharted.terarium.routes;

import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;

@Path("/api/admin")
@Authenticated
public class AdminResource {

	@Inject
	SecurityIdentity identity;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Admin admin() {
		// return "granted";
		return new Admin(identity);
	}

	public static class Admin {

		private final String userName;
		private final Set<String> roles;

		Admin(SecurityIdentity identity) {
			this.userName = identity.getPrincipal().getName();
			this.roles = identity.getRoles();
		}

		public String getAdminName() {
			return userName;
		}

		public Set<String> getRoles() {
			return roles;
		}
	}
}
