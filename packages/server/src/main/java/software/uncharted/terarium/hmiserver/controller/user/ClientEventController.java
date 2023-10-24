package software.uncharted.terarium.hmiserver.controller.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import software.uncharted.terarium.hmiserver.annotations.IgnoreRequestLogging;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.ServerSentEventService;


@RestController
@Slf4j
@RequestMapping("/client-event")
@RequiredArgsConstructor
public class ClientEventController {
  private final ServerSentEventService sseService;
  private final CurrentUserService currentUserService;
  private TaskScheduler taskScheduler;

  @GetMapping
  @IgnoreRequestLogging
  public SseEmitter subscribe() {
    return sseService.connect(currentUserService.get());
  }


}
