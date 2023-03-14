package software.uncharted.terarium.hmiserver.resources.loggingservice;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import software.uncharted.terarium.hmiserver.services.LoggingService;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/api/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@Tag(name = "Model REST Endpoints")
public class LoggingResource {

	@Inject
	SecurityIdentity securityIdentity;

	@Inject
	LoggingService loggingService;

	@POST
	@Path("/logs")
	public Response echoLogs(LoggingService logData) {
		logData.echoLogs(securityIdentity.getPrincipal().getName());
		return Response.ok().build();
	}
}
