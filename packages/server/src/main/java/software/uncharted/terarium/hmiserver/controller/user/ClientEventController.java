package software.uncharted.terarium.hmiserver.controller.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import software.uncharted.terarium.hmiserver.annotations.IgnoreRequestLogging;
import software.uncharted.terarium.hmiserver.service.ClientEventService;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;

@RestController
@Slf4j
@RequestMapping("/client-event")
@RequiredArgsConstructor
public class ClientEventController {

	private final ClientEventService clientEventService;
	private final CurrentUserService currentUserService;

	@GetMapping
	@IgnoreRequestLogging
	public SseEmitter subscribe() {
		return clientEventService.connect(currentUserService.get());
	}
}
