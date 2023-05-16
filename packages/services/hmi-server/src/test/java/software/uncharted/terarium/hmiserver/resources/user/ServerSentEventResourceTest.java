package software.uncharted.terarium.hmiserver.resources.user;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import software.uncharted.terarium.hmiserver.models.EventType;
import software.uncharted.terarium.hmiserver.models.user.User;
import software.uncharted.terarium.hmiserver.services.UserEventService;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.SseEventSource;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static io.restassured.RestAssured.when;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@QuarkusTest
public class ServerSentEventResourceTest {

	@Inject
	UserEventService userEventService;

	private final User alice = new User("Alice");
	private final EventType type = EventType.SEARCH;

	@Test
	@TestHTTPEndpoint(ServerSentEventResource.class)
	public void testIfUserEventAreFilteredByActiveUser() {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target("/server-sent-events");

		List<String> received = new CopyOnWriteArrayList<>();

		SseEventSource source = SseEventSource.target(target).build();
		source.register(inboundSseEvent -> received.add(inboundSseEvent.readData()));
		source.open();
		userEventService.send(type, "Alice", alice);
		userEventService.send(type, "Current");
		await().atMost(100000, MILLISECONDS).until(() -> received.size() == 1);
		source.close();
	}
}
