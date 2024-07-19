package software.uncharted.terarium.hmiserver.proxies;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import software.uncharted.terarium.hmiserver.models.KeycloakTokenRequest;

@FeignClient(
	name = "keycloak",
	url = "${terarium.keycloak.url}/realms/${terarium.keycloak.realm}/protocol/openid-connect"
)
public interface KeycloakProxy {
	@PostMapping(value = "/token", consumes = "application/x-www-form-urlencoded")
	String getToken(@RequestBody KeycloakTokenRequest request);
}
