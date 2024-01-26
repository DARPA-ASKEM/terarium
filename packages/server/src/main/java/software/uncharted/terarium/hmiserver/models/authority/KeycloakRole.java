package software.uncharted.terarium.hmiserver.models.authority;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public enum KeycloakRole {
	USER,
	ADMIN,
	GROUP,
	TEST,
	SERVICE;

  public static KeycloakRole get(final String role) {
		try {
			return valueOf(role.toUpperCase());
		} catch (final IllegalArgumentException e) {
			log.debug("Keycloak role {} not found", role);
			return null;
		}
	}

	public static List<KeycloakRole> get(final List<String> roles) {
		return roles.stream()
			.map(KeycloakRole::get)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}
}
