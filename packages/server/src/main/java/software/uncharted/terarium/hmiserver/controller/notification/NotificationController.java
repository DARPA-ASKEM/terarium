package software.uncharted.terarium.hmiserver.controller.notification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.uncharted.terarium.hmiserver.models.notification.NotificationGroup;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.notification.NotificationService;

@RequestMapping("/notification")
@RestController
@Slf4j
@RequiredArgsConstructor
@Transactional
public class NotificationController {

	private final CurrentUserService currentUserService;
	private final NotificationService notificationService;

	@GetMapping
	@Secured(Roles.USER)
	@Operation(summary = "Return all recent notification groups for a user")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Returned recent notifications successfully",
				content = @Content(
					array = @ArraySchema(
						schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = NotificationGroup.class)
					)
				)
			),
			@ApiResponse(responseCode = "500", description = "There was an issue fetching the notifications")
		}
	)
	public ResponseEntity<List<NotificationGroup>> getNotificationGroups(
		@RequestParam(value = "since", required = false, defaultValue = "48") final long sinceInHours,
		@RequestParam(value = "include-unack", required = false, defaultValue = "false") final boolean includeUnack
	) {
		final LocalDateTime sinceDateTime = LocalDateTime.now().minusHours(sinceInHours);
		final Timestamp since = Timestamp.from(sinceDateTime.atZone(ZoneId.systemDefault()).toInstant());

		final String userId = currentUserService.get().getId().toString();

		if (includeUnack) {
			return ResponseEntity.ok(notificationService.getNotificationGroupsCreatedSinceLatestEventOnly(userId, since));
		}
		return ResponseEntity.ok(
			notificationService.getUnAckedNotificationGroupsCreatedSinceLatestEventOnly(userId, since)
		);
	}

	@PutMapping("/ack/{groupId}")
	@Secured(Roles.USER)
	@Operation(summary = "Acknowledges all events in notification group")
	@ApiResponses(
		value = {
			@ApiResponse(responseCode = "200", description = "Acknowledged all events in notification group"),
			@ApiResponse(responseCode = "500", description = "There was an issue acknowledging the notification group")
		}
	)
	public ResponseEntity<Void> acknowledgeNotificationGroup(@PathVariable("groupId") final UUID groupId) {
		notificationService.acknowledgeNotificationGroup(groupId);

		return ResponseEntity.ok(null);
	}
}
