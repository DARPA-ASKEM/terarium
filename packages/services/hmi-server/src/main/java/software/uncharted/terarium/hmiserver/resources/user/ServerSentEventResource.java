package software.uncharted.terarium.hmiserver.resources.user;


import com.oracle.svm.core.annotate.Inject;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Multi;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.annotations.SseElementType;
import org.reactivestreams.Publisher;
import software.uncharted.terarium.hmiserver.models.user.UserEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.SseEventSink;
import java.util.stream.Stream;

@Path("/api/user/")
@ApplicationScoped
@Authenticated
@Tag(name = "Server Sent Events Endpoints")
public class ServerSentEventResource {

	@Inject
	SecurityIdentity securityIdentity;

	@Inject
	@Channel("user-event") Publisher<UserEvent> userEvents;

	/**
	 * Gets all user events
	 */
	@GET
	@Path("/server-sent-events")
	@Produces(MediaType.SERVER_SENT_EVENTS)
	@SseElementType(MediaType.APPLICATION_JSON)
	public Publisher<UserEvent> stream() {
		return Multi.createFrom().publisher(userEvents);
	}
}
