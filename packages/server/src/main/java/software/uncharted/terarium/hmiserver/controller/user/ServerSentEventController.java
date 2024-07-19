package software.uncharted.terarium.hmiserver.controller.user;

import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.annotations.SseElementType;
import org.reactivestreams.Publisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.uncharted.terarium.hmiserver.models.user.UserEvent;
import software.uncharted.terarium.hmiserver.security.Roles;

@RequestMapping("user/")
@RestController
public class ServerSentEventController {

	// @Autowired
	// @Channel("user-event") Publisher<UserEvent> userEvents;

	/** Gets all user events */
	@GetMapping(name = "/server-sent-events", produces = MediaType.SERVER_SENT_EVENTS)
	@Secured(Roles.USER)
	@SseElementType(MediaType.APPLICATION_JSON)
	public ResponseEntity<Publisher<UserEvent>> stream() {
		return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
		// return Multi.createFrom().publisher(userEvents);
	}
}
