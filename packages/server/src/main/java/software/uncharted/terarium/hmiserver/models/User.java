package software.uncharted.terarium.hmiserver.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.persistence.Id;
import java.io.Serial;
import java.util.Collection;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.Jwt;
import software.uncharted.terarium.hmiserver.annotations.TSIgnore;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.authority.Role;

@Data
@Entity
@Accessors(chain = true)
@Table(
	name = "users" // "user" is a reserved word in many db engines
)
@TSModel
public class User implements UserDetails {

	@Serial
	private static final long serialVersionUID = 2359680472757828388L;

	@Id
	private String id;

	private Long createdAtMs;
	private Long lastLoginAtMs;

	@ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.ALL })
	private Collection<Role> roles;

	private String username;
	private String email;
	private String givenName;
	private String familyName;
	private String name;

	@Transient
	@TSIgnore
	Collection<SimpleGrantedAuthority> authorities;

	@Transient
	@JsonIgnore
	@TSIgnore
	private String password = "";

	@Transient
	@JsonIgnore
	@TSIgnore
	private boolean accountNonExpired = true;

	@Transient
	@JsonIgnore
	@TSIgnore
	private boolean accountNonLocked = true;

	@Transient
	@JsonIgnore
	@TSIgnore
	private boolean credentialsNonExpired = true;

	private boolean enabled = false;

	public static User fromJwt(final Jwt jwt) {
		return new User()
			.setId(jwt.getClaimAsString(StandardClaimNames.SUB))
			.setUsername(jwt.getClaimAsString(StandardClaimNames.PREFERRED_USERNAME))
			.setEmail(jwt.getClaimAsString(StandardClaimNames.EMAIL))
			.setGivenName(jwt.getClaimAsString(StandardClaimNames.GIVEN_NAME))
			.setFamilyName(jwt.getClaimAsString(StandardClaimNames.FAMILY_NAME))
			.setName(jwt.getClaimAsString(StandardClaimNames.NAME));
	}

	/**
	 * Checks if two users are different
	 *
	 * @param a the first user
	 * @param b the second user
	 * @return true if the users are different or either user is null, false otherwise
	 */
	public static boolean isDirty(final User a, final User b) {
		if (a == null || b == null) {
			return true;
		}

		return hash(a) != hash(b);
	}

	/**
	 * Computes a hash of a user
	 *
	 * @param user
	 * @return
	 */
	private static int hash(final User user) {
		return (
			user.id + user.username + user.email + user.givenName + user.familyName + user.name + user.enabled + user.roles
		).hashCode();
	}

	public User merge(final User other) {
		lastLoginAtMs = other.lastLoginAtMs;
		createdAtMs = other.createdAtMs;
		if (roles == null) {
			roles = other.roles;
		} else {
			for (Role role : other.roles) {
				if (!roles.contains(role)) {
					roles.add(role);
				}
			}
		}
		return this;
	}
}
