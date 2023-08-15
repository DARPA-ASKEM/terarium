package software.uncharted.terarium.hmiserver.resources;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import software.uncharted.terarium.hmiserver.models.user.UserEvent;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.UUID;

@Path("/api/dev-tests")
@Tag(name = "DevTest REST Endpoints")
public class DevTestResource {
	@Broadcast
	@Channel("user-event")
	Emitter<UserEvent> userEventEmitter;

	@PUT
	@Path("/user-event")
	@Produces(MediaType.APPLICATION_JSON)
	@Tag(name = "dummy user event")
	public Response createModel() {
		final UUID id = UUID.randomUUID();
		final UserEvent event = new UserEvent();
		event.setId(id);
		userEventEmitter.send(event);
		return Response.ok(Map.of("id", id.toString())).build();
	}
}
