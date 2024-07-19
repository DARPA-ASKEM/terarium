package software.uncharted.terarium.hmiserver.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.models.KeycloakTokenRequest;
import software.uncharted.terarium.hmiserver.proxies.KeycloakProxy;

@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakTokenService {

	private final KeycloakProxy keycloakProxy;
	private final RedissonClient redissonClient;
	private final ObjectMapper objectMapper;
	private final JwtDecoder jwtDecoder;
	private final Config config;
	private RMapCache<String, String> tokenMap;

	@PostConstruct
	public void init() {
		tokenMap = redissonClient.getMapCache("keycloak-tokens");
	}

	/**
	 * Get a token from keycloak. If the token is cached, it will be returned. If the token is expired, it will be
	 * refreshed and returned.
	 *
	 * @param username The username
	 * @param password The password
	 * @return The token if authentication was successful, null otherwise
	 */
	public String getToken(final String username, final String password) {
		// See if we have this token cached. If it's expired, remove it from the cache
		// and get a new one
		String token = tokenMap.get(key(username, password));
		if (token != null && !isValidToken(token)) {
			tokenMap.remove(key(username, password));
			token = null;
		}

		if (token == null) {
			// Fetch a token from keycloak
			try {
				token = keycloakProxy.getToken(
					new KeycloakTokenRequest()
						.setClientId(config.getKeycloak().getClientId())
						.setUsername(username)
						.setPassword(password)
				);
			} catch (final Exception e) {
				log.error("Error getting token from keycloak", e);
				return null;
			}

			// Parse it
			try {
				final JsonNode tokenNode = objectMapper.readTree(token);
				token = tokenNode.get("access_token").asText();
			} catch (final Exception e) {
				log.error("Error parsing token", e);
				return null;
			}

			// If the token has an expiration, cache it
			final Jwt jwt = jwtDecoder.decode(token);
			long expiration = 0;
			if (jwt.getExpiresAt() != null) {
				expiration = jwt.getExpiresAt().toEpochMilli() - Instant.now().toEpochMilli();
			}
			if (expiration > 0) {
				tokenMap.put(key(username, password), token, expiration, TimeUnit.MILLISECONDS);
			}
		}
		return token;
	}

	/**
	 * Gets the map key for the given username and password
	 *
	 * @param username The username
	 * @param password The password
	 * @return The map key
	 */
	private static String key(final String username, final String password) {
		return HashService.sha256(username + password);
	}

	/**
	 * Tests if a token is valid
	 *
	 * @param token the token to test for validity. Obtained from the access_token field of the response from keycloak
	 * @return true if the token is valid, false otherwise
	 */
	private boolean isValidToken(final String token) {
		try {
			final Jwt jwt = jwtDecoder.decode(token);
			// Add a buffer of a second for the request to get to the server
			return (jwt.getExpiresAt() != null && jwt.getExpiresAt().isAfter(Instant.now().minus(1, ChronoUnit.SECONDS)));
		} catch (final Exception e) {
			log.error("Error decoding token", e);
			return false;
		}
	}

	/**
	 * Tests if a token has a given realm role
	 *
	 * @param token the token to test for validity. Obtained from the access_token field of the response from keycloak
	 * @param role the role to test for
	 * @return true if the token has the role, false otherwise
	 */
	public boolean hasRealmRole(final String token, final String role) {
		try {
			final Jwt jwt = jwtDecoder.decode(token);
			// noinspection unchecked
			return ((List<String>) jwt.getClaimAsMap("realm_access").getOrDefault("roles", new ArrayList<>())).contains(role);
		} catch (final Exception e) {
			log.error("Error decoding token", e);
			return false;
		}
	}
}
