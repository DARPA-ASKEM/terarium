package software.uncharted.terarium.hmiserver.service;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.authority.AuthorityLevel;
import software.uncharted.terarium.hmiserver.models.authority.AuthorityType;
import software.uncharted.terarium.hmiserver.models.authority.KeycloakRole;
import software.uncharted.terarium.hmiserver.models.authority.RoleType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DataInitializationService {

	private final AuthorityService authorityService;
	private final RoleService roleService;

	@PostConstruct
	@Transactional(rollbackOn = Exception.class)
	public void initialize() {
		initializeAuthorities();
		initializeRoles();
	}

	private void initializeAuthorities() {
		if (authorityService.count() == 0L) {
			Arrays.stream(AuthorityType.values()).forEach(type -> authorityService.createAuthority(type));
		}
	}

	private void initializeRoles() {
		if (roleService.count() == 0L) {
			roleService.createRole(RoleType.ADMIN, Map.of(
				AuthorityType.GRANT_AUTHORITY, List.of(AuthorityLevel.READ, AuthorityLevel.CREATE, AuthorityLevel.UPDATE, AuthorityLevel.DELETE),
				AuthorityType.USERS, List.of(AuthorityLevel.READ, AuthorityLevel.CREATE, AuthorityLevel.UPDATE, AuthorityLevel.DELETE)
			));

			roleService.createRole(RoleType.USER, Map.of(
				AuthorityType.GRANT_AUTHORITY, List.of(AuthorityLevel.READ),
				AuthorityType.USERS, List.of(AuthorityLevel.READ, AuthorityLevel.UPDATE)
			));
		}
	}

	public Set<RoleType> getRoleTypesForKeycloakRole(final KeycloakRole keycloakRole) {
		return switch (keycloakRole) {
			case ADMIN -> Set.of(RoleType.ADMIN, RoleType.USER);
			case USER -> Set.of(RoleType.USER);
		};
	}
}
