package software.uncharted.terarium.hmiserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.uncharted.terarium.hmiserver.configuration.Config;

@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class ConfigController {
	private final Config config;

	@GetMapping
	public ResponseEntity<Config.ClientConfig> getConfig() {
		return ResponseEntity.ok(config.getClientConfig());
	}
}
