package software.uncharted.terarium.hmiserver.resources.user;


import com.oracle.svm.core.annotate.Inject;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.annotations.SseElementType;
import software.uncharted.terarium.hmiserver.models.user.UserEvent;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/api/user/")
@Authenticated
@Tag(name = "Server Sent Events Endpoints")
public class ServerSentEventResource {

	@Inject
	SecurityIdentity securityIdentity;

	/**
	 * Gets all user events
	 */
	@GET
	@Path("/server-sent-events")
	@Produces(MediaType.SERVER_SENT_EVENTS)
	@SseElementType("text/plain")
	public UserEvent<String> stream() {
		return null; //newUserEvents;
	}
}
