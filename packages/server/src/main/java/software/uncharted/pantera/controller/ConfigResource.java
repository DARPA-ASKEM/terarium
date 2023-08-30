package software.uncharted.pantera.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.uncharted.pantera.configuration.Config;

@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class ConfigResource {
  private final Config config;

  @GetMapping
  public ResponseEntity<Config.ClientConfig> getConfig() {
    return ResponseEntity.ok(config.getClientConfig());
  }
}
