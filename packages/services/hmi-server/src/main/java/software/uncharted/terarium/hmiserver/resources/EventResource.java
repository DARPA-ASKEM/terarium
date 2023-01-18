package software.uncharted.terarium.hmiserver.resources;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import software.uncharted.terarium.hmiserver.entities.Event;
import software.uncharted.terarium.hmiserver.models.EventType;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/api/events")
@Authenticated
@Tag(name = "Events REST Endpoints")
@Slf4j
public class EventResource {
	@Inject
	private SecurityIdentity securityIdentity;

	/**
	 * Gets a list of events sorted by timestamp descending
	 * @param type				the {@link EventType} of the events to fetch
	 * @param projectId		the projectId to fetch events for
	 * @param limit				the number of events to fetch
	 * @return						a list of {@link Event} for the given user/project/type sorted by most to least recent
	 */
	@GET
	public Response getEvents(@QueryParam("type") final EventType type,
														@QueryParam("projectId") final Long projectId,
														@QueryParam("search") final String likeValue,
														@QueryParam("limit") @DefaultValue("10") final int limit) {
		if (type == null || projectId == null) {
			return Response
				.status(Response.Status.BAD_REQUEST)
				.build();
		}

		return Response
			.ok(Event.findAllByTypeAndProjectAndUsernameAndLikeLimit(type, projectId, securityIdentity.getPrincipal().getName(), likeValue, limit))
			.build();
	}

	/**
	 * Create an event
	 * @param event	the {@link Event} instance
	 * @return			the persisted event instance
	 */
	@POST
	@Transactional
	public Response putEvent(final Event event) {
		log.info("Event | " + securityIdentity.getPrincipal().getName() + " | " + event);

		// Do not save the event to the database if the type is not specified as persistent
		if (!event.getType().isPersistent()) {
			return Response
				.ok(null)
				.build();
		}

		event.setUsername(securityIdentity.getPrincipal().getName());
		Event.persist(event);
		return Response
			.ok(event)
			.build();
	}
}
