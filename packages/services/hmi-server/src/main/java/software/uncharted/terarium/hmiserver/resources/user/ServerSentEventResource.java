package software.uncharted.terarium.hmiserver.resources.user;



import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.annotations.SseElementType;

import software.uncharted.terarium.hmiserver.models.user.UserEvent;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.concurrent.Flow;


@Path("/api/user/")
@ApplicationScoped
@Tag(name = "Server Sent Events Endpoints")
public class ServerSentEventResource {

	@Inject
	SecurityIdentity securityIdentity;

	@Inject
	@Channel("user-event")
	Flow.Publisher<UserEvent> userEvents;

	/**
	 * Gets all user events
	 */
	@GET
	@Path("/server-sent-events")
	@Produces(MediaType.SERVER_SENT_EVENTS)
	@SseElementType(MediaType.APPLICATION_JSON)
	public Flow.Publisher<UserEvent> stream() {
		return Multi.createFrom().publisher(userEvents);
	}
}
