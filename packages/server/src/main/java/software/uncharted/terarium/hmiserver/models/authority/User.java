package software.uncharted.terarium.hmiserver.models.authority;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.Jwt;
import software.uncharted.terarium.hmiserver.annotations.TSIgnore;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.configuration.SimpleGrantedAuthorityDeserializer;

@Data
@Entity
@Accessors(chain = true)
@Table(
	name = "users" // "user" is a reserved word in many db engines
)
@TSModel
public class User implements UserDetails {

	@Id
	private String id;

	private Long createdAtMs;
	private Long lastLoginAtMs;
	private Long lastUpdateAtMs;

	@ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.ALL })
	private Collection<Role> roles;

	@ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.ALL })
	private Collection<Group> groups;

	@JsonIgnore
	@Transient
	public Collection<Role> getAllRoles() {
		final Collection<Role> combinedRoles = new HashSet<>();

		final Collection<Role> userRoles = getRoles();
		if (userRoles != null && !userRoles.isEmpty()) {
			combinedRoles.addAll(userRoles);
		}
		if (getGroups() != null) {
			final Collection<Role> groupRoles = getGroups()
				.stream()
				.map(Group::getRoles)
				.filter(Objects::nonNull)
				.flatMap(Collection::stream)
				.map(role -> role.setInherited(true))
				.collect(Collectors.toSet());

			if (!groupRoles.isEmpty()) {
				combinedRoles.addAll(groupRoles);
			}
		}
		return combinedRoles;
	}

	private String username;
	private String email;
	private String givenName;
	private String familyName;
	private String name;

	@Transient
	@TSIgnore
	@JsonDeserialize(using = SimpleGrantedAuthorityDeserializer.class)
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

	public static User fromJwt(Jwt jwt) {
		return new User()
			.setId(jwt.getClaimAsString(StandardClaimNames.SUB))
			.setUsername(jwt.getClaimAsString(StandardClaimNames.PREFERRED_USERNAME))
			.setEmail(jwt.getClaimAsString(StandardClaimNames.EMAIL))
			.setGivenName(jwt.getClaimAsString(StandardClaimNames.GIVEN_NAME))
			.setFamilyName(jwt.getClaimAsString(StandardClaimNames.FAMILY_NAME))
			.setName(jwt.getClaimAsString(StandardClaimNames.NAME))
			.setPicture(jwt.getClaimAsString(StandardClaimNames.PICTURE));
	}

	/**
	 * Checks if two users are different
	 *
	 * @param a the first user
	 * @param b the second user
	 * @return true if the users are different, false otherwise
	 */
	public static boolean isDirty(User a, User b) {
		return hash(a) != hash(b);
	}

	/**
	 * Computes a hash of a user
	 *
	 * @param user
	 * @return
	 */
	private static int hash(User user) {
		return (
			user.id + user.username + user.email + user.givenName + user.familyName + user.name + user.enabled + user.picture
		).hashCode();
	}

	public User merge(final User other) {
		lastLoginAtMs = other.lastLoginAtMs;
		createdAtMs = other.createdAtMs;
		lastUpdateAtMs = other.lastUpdateAtMs;
		roles = other.roles;
		hasAvatar = other.hasAvatar;
		groups = other.groups;
		return this;
	}

	/**
	 * Checks if a user is in a group by name
	 *
	 * @param name the name of the group
	 * @return true if the user is in the group, false otherwise
	 */
	public boolean isInGroupByName(String name) {
		if (getGroups() == null) {
			return false;
		}
		return getGroups().stream().map(Group::getName).anyMatch(groupName -> groupName.equals(name));
	}

	/**
	 * Checks if a user is in a group
	 *
	 * @param group the group
	 * @return true if the user is in the group, false otherwise
	 */
	public boolean isInGroup(Group group) {
		return isInGroupByName(group.getName());
	}

	/**
	 * Checks if a user is in a collection of groups by names
	 *
	 * @param names the names of the groups
	 * @return true if the user is in all the groups, false otherwise
	 */
	public boolean isInAllGroupsByName(Collection<String> names) {
		return names.stream().allMatch(this::isInGroupByName);
	}

	/**
	 * Checks if a user is in a collection of groups
	 *
	 * @param groups the groups
	 * @return true if the user is in all the groups, false otherwise
	 */
	public boolean isInAllGroups(Collection<Group> groups) {
		return groups.stream().allMatch(this::isInGroup);
	}

	/**
	 * Checks if a user is in any of a collection of groups by names
	 *
	 * @param groups the names of the groups
	 * @return true if the user is in any of the groups, false otherwise
	 */
	public boolean isInAnyGroup(Collection<Group> groups) {
		return groups.stream().anyMatch(this::isInGroup);
	}

	/**
	 * Checks if a user is in any of a collection of groups
	 *
	 * @param names the names of the groups
	 * @return true if the user is in any of the groups, false otherwise
	 */
	public boolean isInAnyGroupByName(Collection<String> names) {
		return names.stream().anyMatch(this::isInGroupByName);
	}

	/**
	 * Checks if a user has a role
	 *
	 * @param role the role
	 * @return true if the user has the role, false otherwise
	 */
	public boolean hasRole(Role role) {
		return hasRoleByName(role.getName());
	}

	/**
	 * Checks if a user has a role by name
	 *
	 * @param name the name of the role
	 * @return true if the user has the role, false otherwise
	 */
	public boolean hasRoleByName(String name) {
		if (getAllRoles().isEmpty()) {
			return false;
		}
		return getAllRoles().stream().anyMatch(r -> r.getName().equals(name));
	}
}
