package software.uncharted.terarium.hmiserver;

import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.time.Instant;

@Provider
@Slf4j
public class RequestLoggingRequestFilter implements ContainerRequestFilter {
	public static final String REQUEST_START_TIMESTAMP_MS = "request-start-timestamp-ms";

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		final String user = requestContext.getSecurityContext().getUserPrincipal() != null ? requestContext.getSecurityContext().getUserPrincipal().getName() : "Anonymous";
		log.info("REQUEST STARTED | " + requestContext.getRequest().getMethod() + " | " + requestContext.getUriInfo().getRequestUri() + " | " + user);
		requestContext.setProperty(REQUEST_START_TIMESTAMP_MS, Instant.now().toEpochMilli());
	}
}
