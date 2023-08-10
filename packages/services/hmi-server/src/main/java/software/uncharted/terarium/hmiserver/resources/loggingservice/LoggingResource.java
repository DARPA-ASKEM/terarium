package software.uncharted.terarium.hmiserver.resources.loggingservice;

import io.quarkus.security.identity.SecurityIdentity;
import software.uncharted.terarium.hmiserver.annotations.IgnoreRequestLogging;
import software.uncharted.terarium.hmiserver.services.LogMessage;
import software.uncharted.terarium.hmiserver.services.LoggingService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Path("/api")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LoggingResource {

	@Inject
	SecurityIdentity securityIdentity;

	@POST
	@Path("/logs")
	@IgnoreRequestLogging
	public Response echoLogs(LoggingService logData) {
		for (LogMessage log : logData.getLogs()) {
			logData.logMessage(log, securityIdentity.getPrincipal().getName());
		}
		return Response.ok().build();
	}
}

