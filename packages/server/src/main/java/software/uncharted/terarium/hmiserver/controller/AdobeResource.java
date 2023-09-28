package software.uncharted.terarium.hmiserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RequestMapping("/adobe")
@RestController
@Slf4j
public class AdobeResource {

	@Value("${adobe.api-key}")
	String key;

	@GetMapping
	public ResponseEntity<String> getKey() {
		if (key != null) {
			return ResponseEntity.ok(key);
		} else {
			log.error("No Adobe API key is configured");
			return ResponseEntity.internalServerError().build();
		}
	}
}
