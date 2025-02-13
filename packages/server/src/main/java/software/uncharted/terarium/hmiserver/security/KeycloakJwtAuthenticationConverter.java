package software.uncharted.terarium.hmiserver.security;

import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.authority.KeycloakRole;
import software.uncharted.terarium.hmiserver.models.authority.Role;
import software.uncharted.terarium.hmiserver.models.authority.RoleType;
import software.uncharted.terarium.hmiserver.models.authority.User;
import software.uncharted.terarium.hmiserver.service.AdminClientService;
import software.uncharted.terarium.hmiserver.service.DataInitializationService;
import software.uncharted.terarium.hmiserver.service.RoleService;
import software.uncharted.terarium.hmiserver.service.UserService;

@Service
@RequiredArgsConstructor
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

	private final UserService userService;

	private final RoleService roleService;

	private final DataInitializationService dataInitializationService;

	private final AdminClientService adminClientService;

	@Override
	public AbstractAuthenticationToken convert(final Jwt source) {
		return new JwtAuthenticationToken(
			source,
			Stream.concat(
				new JwtGrantedAuthoritiesConverter().convert(source).stream(),
				extractResourceRoles(source).stream()
			).collect(toSet())
		);
	}

	@SuppressWarnings("unchecked")
	private Collection<SimpleGrantedAuthority> extractResourceRoles(final Jwt jwt) {
		// Extract the roles from keycloak itself
		final List<String> realmRoles = (List<String>) jwt
			.getClaimAsMap("realm_access")
			.getOrDefault("roles", new ArrayList<>());

		// Merge with existing roles (or create the new user if this is first login
		final String userId = jwt.getClaimAsString(StandardClaimNames.SUB);
		User databaseUser = userService.getById(userId);
		final User jwtUser = initializeUser(jwt, realmRoles);
		if (User.isDirty(databaseUser, jwtUser)) {
			if (databaseUser == null) {
				databaseUser = userService.save(jwtUser);
			} else {
				databaseUser = userService.save(jwtUser.merge(databaseUser));
			}
		}

		return RoleService.getAuthorities(databaseUser.getRoles())
			.stream()
			.map(SimpleGrantedAuthority::new)
			.collect(Collectors.toSet());
	}

	public User initializeUser(final Jwt jwt, final List<String> keycloakRoles) {
		final User user = adminClientService.getUserFromJwt(jwt);

		final Set<RoleType> roleTypes = KeycloakRole.get(keycloakRoles)
			.stream()
			.map(keycloakRole -> DataInitializationService.getRoleTypesForKeycloakRole(keycloakRole))
			.flatMap(Collection::stream)
			.collect(toSet());

		final List<Role> roles = roleService.getAllByTypes(roleTypes.stream().map(Enum::name).collect(toSet()));

		user.setRoles(roles);

		return UserService.createUser(user);
	}
}
