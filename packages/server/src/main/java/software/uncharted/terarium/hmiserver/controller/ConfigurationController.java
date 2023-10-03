package software.uncharted.terarium.hmiserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This resource is unauthenticated and is used to provide any configuration to the client
 * that must occur before auth is setup.  This should _only_ be used for configuration that
 * is not a secret.
 */

@RequestMapping("/configuration")
@Slf4j
@Configuration
@RestController
public class ConfigurationController {

	@Value("${google-analytics-id}")
	String googleAnalyticsId;

	@GetMapping("/ga")
	public ResponseEntity<String> getGA() {
		if (googleAnalyticsId != null) {
			return ResponseEntity.ok(googleAnalyticsId);
		} else {
			log.warn("No GA key is configured");
			return ResponseEntity.ok("test");
		}
	}
}
