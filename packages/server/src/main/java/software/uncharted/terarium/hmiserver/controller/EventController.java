package software.uncharted.terarium.hmiserver.controller;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.annotations.IgnoreRequestLogging;
import software.uncharted.terarium.hmiserver.models.EventType;
import software.uncharted.terarium.hmiserver.models.user.Event;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.EventService;

@RequestMapping("/events")
@RestController
@Slf4j
@RequiredArgsConstructor
public class EventController {

	private final EventService eventService;

	private final CurrentUserService currentUserService;

	/**
	 * Gets a list of events sorted by timestamp descending
	 *
	 * @param type the {@link EventType} of the events to fetch
	 * @param projectId the projectId to fetch events for
	 * @param limit the number of events to fetch
	 * @return a list of {@link Event} for the given user/project/type sorted by most to least recent
	 */
	@GetMapping
	@Secured(Roles.USER)
	public ResponseEntity<List<Event>> getEvents(
		@RequestParam(value = "type") final EventType type,
		@RequestParam(value = "projectId", required = false) final UUID projectId,
		@RequestParam(value = "search", required = false) final String likeValue,
		@RequestParam(value = "limit", defaultValue = "10") final int limit
	) {
		return ResponseEntity.ok(
			eventService.findEvents(type, projectId, currentUserService.get().getId(), likeValue, limit)
		);
	}

	/**
	 * Create an event
	 *
	 * @param event the {@link Event} instance
	 * @return the persisted event instance
	 */
	@PostMapping
	@Secured(Roles.USER)
	@IgnoreRequestLogging
	public ResponseEntity<Event> postEvent(@RequestBody final Event event) {
		event.setUserId(currentUserService.get().getId());

		// Do not save the event to the database if the type is not specified as persistent
		if (!event.getType().isPersistent()) {
			return ResponseEntity.ok(null);
		}

		return ResponseEntity.ok(eventService.save(event));
	}
}
