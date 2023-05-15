package software.uncharted.terarium.hmiserver.resources.loggingservice;

import io.quarkus.security.identity.SecurityIdentity;
import software.uncharted.terarium.hmiserver.services.LogMessage;
import software.uncharted.terarium.hmiserver.services.LoggingService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/api")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LoggingResource {

	@Inject
	SecurityIdentity securityIdentity;

	@POST
	@Path("/logs")
	public Response echoLogs(LoggingService logData) {
		for (LogMessage log : logData.getLogs()) {
			logData.logMessage(log, securityIdentity.getPrincipal().getName());
		}
		return Response.ok().build();
	}
}

