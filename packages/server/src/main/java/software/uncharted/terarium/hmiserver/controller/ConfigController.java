package software.uncharted.terarium.hmiserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.security.Roles;

@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class ConfigController {

	private final Config config;

	@GetMapping
	@Secured(Roles.USER)
	public ResponseEntity<Config.ClientConfig> getConfig() {
		return ResponseEntity.ok(config.getClientConfig());
	}
}
