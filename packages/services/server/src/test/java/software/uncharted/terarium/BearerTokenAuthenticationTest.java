package org.acme.security.openid.connect;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class BearerTokenAuthenticationTest {

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    @Test
    public void testAdminAccess() {
        RestAssured.given().auth().oauth2(getAccessToken("adam", "asdf1ASDF"))
                .when().get("/api/admin")
                .then()
                .statusCode(200);
        RestAssured.given().auth().oauth2(getAccessToken("ursula", "asdf1ASDF"))
                .when().get("/api/admin")
                .then()
                .statusCode(403);
    }

    protected String getAccessToken(String userName, String userSecret) {
        return keycloakClient.getAccessToken(userName, userSecret, "app", "jtbQhs6SlfynqJaygVpwav2kLzAme2b4");
    }
}

