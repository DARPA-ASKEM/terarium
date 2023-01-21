package software.uncharted.terarium.hmiserver;

import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.time.Instant;

@Provider
@Slf4j
public class RequestLoggingResponseFilter implements ContainerResponseFilter {
	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		final String user = requestContext.getSecurityContext().getUserPrincipal() != null ? requestContext.getSecurityContext().getUserPrincipal().getName() : "Anonymous";
		final long durationMs = Instant.now().toEpochMilli() - (long)requestContext.getProperty(RequestLoggingRequestFilter.REQUEST_START_TIMESTAMP_MS);
		log.info("REQUEST COMPLETED | " + requestContext.getRequest().getMethod() + " | " + requestContext.getUriInfo().getRequestUri() + " | " + user + " | " + durationMs + "ms");
	}
}
