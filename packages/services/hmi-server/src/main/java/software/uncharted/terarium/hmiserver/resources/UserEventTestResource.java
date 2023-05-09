package software.uncharted.terarium.hmiserver.resources;

import io.quarkus.security.Authenticated;
import software.uncharted.terarium.hmiserver.services.UserEventService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/api/test_user_events")
@Authenticated
public class UserEventTestResource {
	@Inject
	UserEventService userEventService;

	@GET
	public Response testUserEvents() {
		return Response.ok(userEventService.generateUserEvent()).build();
	}
}
