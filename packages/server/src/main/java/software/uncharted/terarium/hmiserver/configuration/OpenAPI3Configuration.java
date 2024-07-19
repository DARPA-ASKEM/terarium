package software.uncharted.terarium.hmiserver.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
	servers = { @Server(url = "${terarium.swagger.server-url}") },
	info = @Info(
		title = "Terarium APIs",
		description = "This lists all the Terarium HMI API Calls. The Calls are OAuth2 secured, " +
		"so please use your client ID and Secret to test them out.",
		version = "v1.0"
	),
	security = @SecurityRequirement(name = "security_auth")
)
@SecurityScheme(
	name = "security_auth",
	type = SecuritySchemeType.OAUTH2,
	bearerFormat = "JWT",
	in = SecuritySchemeIn.HEADER,
	flows = @OAuthFlows(
		authorizationCode = @OAuthFlow(
			authorizationUrl = "${terarium.keycloak.url}/realms/${terarium.keycloak.realm}/protocol/openid-connect/auth",
			tokenUrl = "${terarium.keycloak.url}/realms/${terarium.keycloak.realm}/protocol/openid-connect/token",
			refreshUrl = "${terarium.keycloak.url}/realms/${terarium.keycloak.realm}/protocol/openid-connect/token"
		)
	)
)
public class OpenAPI3Configuration {}
