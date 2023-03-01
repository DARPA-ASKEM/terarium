package software.uncharted.terarium.hmiserver.configuration;

import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.util.JsonString;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Provider
@Slf4j
public class RequestLoggingResponseFilter implements ContainerResponseFilter {
	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		final String user = requestContext.getSecurityContext().getUserPrincipal() != null ? requestContext.getSecurityContext().getUserPrincipal().getName() : "Anonymous";
		final long durationMs = Instant.now().toEpochMilli() - (long)requestContext.getProperty(RequestLoggingRequestFilter.REQUEST_START_TIMESTAMP_MS);
		log.info(JsonString.write(Map.of(
			"log_message", "REQUEST COMPLETED",
			"method", requestContext.getRequest().getMethod(),
			"uri", requestContext.getUriInfo().getPath(),
			"user", user,
			"duration", durationMs))
		);
	}
}
