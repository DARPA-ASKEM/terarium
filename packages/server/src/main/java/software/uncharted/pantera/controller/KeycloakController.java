package software.uncharted.pantera.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.uncharted.pantera.configuration.Config;
import software.uncharted.pantera.configuration.KeycloakJsConfiguration;

@RequestMapping("/keycloak")
@RestController
@RequiredArgsConstructor
public class KeycloakController {
  private final Config config;

  @GetMapping("/config")
  ResponseEntity<KeycloakJsConfiguration> keycloak() {
    return ResponseEntity.ok(new KeycloakJsConfiguration()
      .setUrl(config.getKeycloak().getUrl())
      .setRealm(config.getKeycloak().getRealm())
      .setClientId(config.getKeycloak().getClientId())
    );
  }
}
