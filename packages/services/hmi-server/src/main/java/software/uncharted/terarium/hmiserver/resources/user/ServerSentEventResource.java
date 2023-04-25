package software.uncharted.terarium.hmiserver.resources.user;


import com.oracle.svm.core.annotate.Inject;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Multi;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.reactive.messaging.Channel;
import software.uncharted.terarium.hmiserver.models.user.UserEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/api/user/")
@ApplicationScoped
@Authenticated
@Tag(name = "Server Sent Events Endpoints")
public class ServerSentEventResource {

	@Inject
	SecurityIdentity securityIdentity;

	@Channel("user-ack") Multi<UserEvent> userEvents;

	/**
	 * Gets all user events
	 */
	@GET
	@Path("/server-sent-events")
	@Produces(MediaType.SERVER_SENT_EVENTS)
	public Multi<UserEvent> stream() {
		return userEvents;
	}
}
