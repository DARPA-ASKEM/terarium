package software.uncharted.terarium.hmiserver.service;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.authority.AuthorityLevel;
import software.uncharted.terarium.hmiserver.models.authority.AuthorityType;
import software.uncharted.terarium.hmiserver.models.authority.KeycloakRole;
import software.uncharted.terarium.hmiserver.models.authority.Role;
import software.uncharted.terarium.hmiserver.models.authority.RoleType;

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
		final List<RoleType> roleTypesMissing = new ArrayList<>();
		for (final RoleType roleType : RoleType.values()) {
			final List<Role> res = roleService.getAllByTypes(new HashSet<>(List.of(roleType.name())));
			if (res.isEmpty()) {
				roleTypesMissing.add(roleType);
			}
		}

		for (final RoleType roleType : roleTypesMissing) {
			switch (roleType) {
				case ADMIN -> roleService.createRole(
					RoleType.ADMIN,
					Map.of(
						AuthorityType.GRANT_AUTHORITY,
						List.of(AuthorityLevel.READ, AuthorityLevel.CREATE, AuthorityLevel.UPDATE, AuthorityLevel.DELETE),
						AuthorityType.USERS,
						List.of(AuthorityLevel.READ, AuthorityLevel.CREATE, AuthorityLevel.UPDATE, AuthorityLevel.DELETE)
					)
				);
				case USER -> roleService.createRole(
					RoleType.USER,
					Map.of(
						AuthorityType.GRANT_AUTHORITY,
						List.of(AuthorityLevel.READ),
						AuthorityType.USERS,
						List.of(AuthorityLevel.READ, AuthorityLevel.UPDATE)
					)
				);
				case GROUP -> roleService.createRole(
					RoleType.GROUP,
					Map.of(
						AuthorityType.GRANT_AUTHORITY,
						List.of(AuthorityLevel.READ),
						AuthorityType.USERS,
						List.of(AuthorityLevel.READ, AuthorityLevel.UPDATE)
					)
				);
				case TEST -> roleService.createRole(
					RoleType.TEST,
					Map.of(
						AuthorityType.GRANT_AUTHORITY,
						List.of(AuthorityLevel.READ),
						AuthorityType.USERS,
						List.of(AuthorityLevel.READ, AuthorityLevel.CREATE, AuthorityLevel.UPDATE, AuthorityLevel.DELETE)
					)
				);
				case SERVICE -> roleService.createRole(
					RoleType.SERVICE,
					Map.of(
						AuthorityType.GRANT_AUTHORITY,
						List.of(AuthorityLevel.READ),
						AuthorityType.USERS,
						List.of(AuthorityLevel.READ, AuthorityLevel.CREATE, AuthorityLevel.UPDATE, AuthorityLevel.DELETE)
					)
				);
			}
		}
	}

	public static Set<RoleType> getRoleTypesForKeycloakRole(final KeycloakRole keycloakRole) {
		return switch (keycloakRole) {
			case ADMIN -> Set.of(RoleType.ADMIN, RoleType.USER);
			case USER -> Set.of(RoleType.USER);
			case GROUP -> Set.of(RoleType.GROUP);
			case TEST -> Set.of(RoleType.TEST);
			case SERVICE -> Set.of(RoleType.SERVICE);
		};
	}
}
