package software.uncharted.terarium.hmiserver.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

@Slf4j
public class LoggingAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(
		final HttpServletRequest request,
		final HttpServletResponse response,
		final AuthenticationException authException
	) throws IOException, ServletException {
		log.warn("Unauthorized warning: {}", authException.getMessage());
		log.warn("The Unauthorized request has the following headers:");
		request
			.getHeaderNames()
			.asIterator()
			.forEachRemaining(headerName -> {
				log.warn("\tHeader: {} = {}", headerName, request.getHeader(headerName));
			});
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
	}
}
