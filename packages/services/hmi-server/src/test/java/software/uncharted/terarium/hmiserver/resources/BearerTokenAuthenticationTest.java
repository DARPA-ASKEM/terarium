package software.uncharted.terarium.hmiserver.resources;

import io.quarkus.test.junit.QuarkusTest;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import software.uncharted.terarium.hmiserver.auth.KeycloakTestUtil;

@QuarkusTest
public class BearerTokenAuthenticationTest {

    KeycloakTestUtil keycloak = new KeycloakTestUtil();

//    @Test
//    public void testAdminAccess() {
//        RestAssured.given().auth().oauth2(keycloak.getAccessToken("adam", "asdf1ASDF"))
//                .when().get("/api/admin")
//                .then()
//                .statusCode(200);
//        RestAssured.given().auth().oauth2(keycloak.getAccessToken("ursula", "asdf1ASDF"))
//                .when().get("/api/admin")
//                .then()
//                .statusCode(403);
//    }
}

