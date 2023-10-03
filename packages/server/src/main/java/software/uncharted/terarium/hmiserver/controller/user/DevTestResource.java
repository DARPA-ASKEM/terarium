package software.uncharted.terarium.hmiserver.controller.user;

import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.uncharted.terarium.hmiserver.models.user.UserEvent;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.UUID;

@RequestMapping("/dev-tests")
@RestController
public class DevTestResource {
	//@Broadcast
	//@Channel("user-event")
	//Emitter<UserEvent> userEventEmitter;

	@PutMapping("/user-event")

	public ResponseEntity<JsonNode> createModel() {
		final UUID id = UUID.randomUUID();
		final UserEvent event = new UserEvent();
		//event.setId(id);
		//userEventEmitter.send(event);
		//return Response.ok(Map.of("id", id.toString())).build();
		return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
	}
}
