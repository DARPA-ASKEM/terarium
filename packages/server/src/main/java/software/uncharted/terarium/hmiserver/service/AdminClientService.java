package software.uncharted.terarium.hmiserver.service;

import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.models.CacheName;
import software.uncharted.terarium.hmiserver.models.authority.User;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = CacheName.USER_REPRESENTATION)
public class AdminClientService {

	private final Config config;

	private Keycloak keycloak = null;

	@PostConstruct
	public void init() {
		// todo configify
		keycloak = KeycloakBuilder.builder()
			.serverUrl(config.getKeycloak().getUrl())
			.realm(config.getKeycloak().getAdminRealm())
			.clientId(config.getKeycloak().getAdminClientId())
			.grantType(OAuth2Constants.PASSWORD)
			.username(config.getKeycloak().getAdminUsername())
			.password(config.getKeycloak().getAdminPassword())
			.build();
	}

	@Cacheable(key = "#jwt.subject")
	public User getUserFromJwt(final Jwt jwt) {
		final var user = User.fromJwt(jwt);
		final var representation = getUserRepresentationById(user.getId());
		user.setEnabled(representation.isEnabled()); // for now.
		return user;
	}

	private UserResource getUserResource(final String id) {
		return keycloak.realm(config.getKeycloak().getRealm()).users().get(id);
	}

	private UserRepresentation getUserRepresentationById(final String id) {
		return getUserResource(id).toRepresentation();
	}

	// Updates the user representation in keycloak based on our internal user model
	@CacheEvict(key = "#user.getId()")
	public Boolean updateUserRepresentation(final User user) {
		try {
			final var resource = getUserResource(user.getId());
			final var representation = resource.toRepresentation();

			// update fields here
			representation.setEmail(user.getEmail());
			representation.setFirstName(user.getGivenName());
			representation.setLastName(user.getFamilyName());
			representation.setUsername(user.getUsername());
			resource.update(representation);
			return true;
		} catch (final Exception e) {
			return false;
		}
	}
}
