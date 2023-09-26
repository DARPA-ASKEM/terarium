package software.uncharted.terarium.hmiserver.controller.logging;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.uncharted.terarium.hmiserver.annotations.IgnoreRequestLogging;
import software.uncharted.terarium.hmiserver.controller.services.IAuthenticationFacade;
import software.uncharted.terarium.hmiserver.controller.services.LogMessage;
import software.uncharted.terarium.hmiserver.controller.services.LoggingService;

import javax.ws.rs.core.Response;


@RequestMapping("/logsOld")
@RestController
@Slf4j
public class LogsController {

	@Autowired
	private IAuthenticationFacade authenticationFacade;

	@PostMapping
	@IgnoreRequestLogging
	public Response echoLogs(LoggingService logData) {

		for (LogMessage log : logData.getLogs()) {
			logData.logMessage(log, authenticationFacade.getAuthentication().getName());
		}
		return Response.ok().build();
	}
}

