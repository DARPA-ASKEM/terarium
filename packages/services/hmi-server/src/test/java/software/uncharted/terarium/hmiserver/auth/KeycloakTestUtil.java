package software.uncharted.terarium.hmiserver.auth;

import io.quarkus.test.keycloak.client.KeycloakTestClient;


public class KeycloakTestUtil extends KeycloakTestClient {

	public KeycloakTestUtil() {
		super();
	}

	public String getAccessToken(String userName, String userSecret) {
		return this.getAccessToken(userName, userSecret, "app", "jtbQhs6SlfynqJaygVpwav2kLzAme2b4");
	}
}
