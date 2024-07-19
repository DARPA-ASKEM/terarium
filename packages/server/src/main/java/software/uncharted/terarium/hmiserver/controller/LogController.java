package software.uncharted.terarium.hmiserver.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.uncharted.terarium.hmiserver.models.ClientLog;
import software.uncharted.terarium.hmiserver.models.User;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;

@RestController
@RequestMapping("/logs")
@Slf4j
@RequiredArgsConstructor
public class LogController {

	private final CurrentUserService currentUserService;

	/**
	 * Log a list of messages from the client
	 *
	 * @param logList the list of messages to log
	 */
	@PostMapping
	@Secured(Roles.USER)
	public ResponseEntity<HttpStatus> log(@RequestBody List<ClientLog> logList) {
		logList.forEach(clientLog -> {
			List<String> parts = new ArrayList<>(
				List.of(clientLog.getTimestampMillis() + "", getCurrentUserId(), clientLog.getMessage())
			);
			if (clientLog.getArgs() != null && clientLog.getArgs().length > 0) {
				parts.addAll(Arrays.asList(clientLog.getArgs()));
			}
			String message = "CLIENT_LOG | " + String.join(" | ", parts);
			switch (clientLog.getLevel()) {
				case "error" -> log.error(message);
				case "warn" -> log.warn(message);
				case "debug" -> log.debug(message);
				default -> log.info(message);
			}
		});
		return ResponseEntity.ok(HttpStatus.OK);
	}

	/**
	 * Get the current user id, or "Anonymous" if no user is logged in (eg/ this is a log message from a system or
	 * scheduled event)
	 *
	 * @return the current user id
	 */
	private String getCurrentUserId() {
		final User user = currentUserService.get();
		if (user == null) {
			return "Anonymous";
		}
		return user.getId();
	}
}
