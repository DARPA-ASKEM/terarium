package software.uncharted.terarium.hmiserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.uncharted.terarium.hmiserver.security.Roles;

@RequestMapping("/adobe")
@RestController
@Slf4j
public class AdobeController {

	@Value("${adobe.api-key}")
	String adobeKey;

	@Value("${adobe.api-localhost-key}")
	String adobeLocalhostKey;

	@GetMapping
	@Secured(Roles.USER)
	public ResponseEntity<String> getKey(@RequestHeader(value = "Referer", required = false) String referer) {
		final String key = referer != null && referer.contains("localhost") ? adobeLocalhostKey : adobeKey;
		if (key == null) {
			log.error("No Adobe API key is configured");
			return ResponseEntity.internalServerError().build();
		}
		return ResponseEntity.ok(key);
	}
}
