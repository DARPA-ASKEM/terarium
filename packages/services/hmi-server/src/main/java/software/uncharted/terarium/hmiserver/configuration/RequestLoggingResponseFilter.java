package software.uncharted.terarium.hmiserver.configuration;

import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.services.StructuredLog;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.time.Instant;

@Provider
@Slf4j
public class RequestLoggingResponseFilter implements ContainerResponseFilter {
	@Inject
	StructuredLog structuredLog;

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		final boolean shouldLog = requestContext.getProperty(RequestLoggingRequestFilter.REQUEST_START_TIMESTAMP_MS) != null;
		if (shouldLog) {
			final String user = requestContext.getSecurityContext().getUserPrincipal() != null ? requestContext.getSecurityContext().getUserPrincipal().getName() : "Anonymous";
			final long durationMs = Instant.now().toEpochMilli() - (long) requestContext.getProperty(RequestLoggingRequestFilter.REQUEST_START_TIMESTAMP_MS);
			structuredLog.log(StructuredLog.Type.REQUEST_COMPLETED, user,
				"uri", requestContext.getUriInfo().getPath(),
				"method", requestContext.getRequest().getMethod(),
				"duration", durationMs
			);
		}
	}
}
