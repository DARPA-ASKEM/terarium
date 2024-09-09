package software.uncharted.terarium.hmiserver.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

@Slf4j
public class LoggingAccessDeniedHandler implements AccessDeniedHandler {

	@Override
	public void handle(
		final HttpServletRequest request,
		final HttpServletResponse response,
		final AccessDeniedException accessDeniedException
	) throws IOException, ServletException {
		log.warn("Access Denied warning: {}", accessDeniedException.getMessage());
		log.warn("The Denied request has the following headers:");
		request
			.getHeaderNames()
			.asIterator()
			.forEachRemaining(headerName -> {
				log.warn("\tHeader: {} = {}", headerName, request.getHeader(headerName));
			});
		response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
	}
}
