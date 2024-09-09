package software.uncharted.terarium.hmiserver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.KeycloakJsConfiguration;

/**
 * This resource is unauthenticated and is used to provide any configuration to the client that must occur before auth
 * is setup. This should _only_ be used for configuration that is not a secret.
 */
@RequestMapping("/configuration")
@Slf4j
@Configuration
@RestController
@RequiredArgsConstructor
public class ConfigurationController {

	@Value("${google-analytics-id}")
	String googleAnalyticsId;

	private final Config config;

	@GetMapping("/ga")
	public ResponseEntity<String> getGA() {
		if (googleAnalyticsId != null) {
			return ResponseEntity.ok(googleAnalyticsId);
		} else {
			log.warn("No GA key is configured");
			return ResponseEntity.ok("test");
		}
	}

	@GetMapping("/keycloak")
	ResponseEntity<KeycloakJsConfiguration> getKeycloak() {
		KeycloakJsConfiguration keycloakJsConfiguration = new KeycloakJsConfiguration()
			.setUrl(config.getKeycloak().getUrl())
			.setRealm(config.getKeycloak().getRealm())
			.setClientId(config.getKeycloak().getClientId());

		return ResponseEntity.ok(keycloakJsConfiguration);
	}
}
