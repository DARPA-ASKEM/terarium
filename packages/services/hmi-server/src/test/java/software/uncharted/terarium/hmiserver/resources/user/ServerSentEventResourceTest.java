package software.uncharted.terarium.hmiserver.resources.user;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import software.uncharted.terarium.hmiserver.models.EventType;
import software.uncharted.terarium.hmiserver.models.user.User;
import software.uncharted.terarium.hmiserver.services.UserEventService;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
public class ServerSentEventResourceTest {

	@Inject
	UserEventService userEventService;

	private final User alice = new User("Alice");
	private final EventType type = EventType.SEARCH;

	@Test
	@TestHTTPEndpoint(ServerSentEventResource.class)
	public void testIfUserEventAreFilteredByActiveUser() {
		// Send a couple of message for Alice and the current user
		userEventService.send(type, "Alice", alice);
		userEventService.send(type, "Alice", alice);
		userEventService.send(type, "Current");
		userEventService.send(type, "Current");

		when().get("/server-sent-events")
			.then()
			.statusCode(200)
			.contentType(MediaType.SERVER_SENT_EVENTS)
			.body(containsString("Current"));
	}
}
