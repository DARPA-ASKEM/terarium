package software.uncharted.terarium.hmiserver.security;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import software.uncharted.terarium.hmiserver.configuration.Config;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnauthenticatedUrlRequestMatcher implements RequestMatcher {

	private final Config config;

	private RequestMatcher pathMatcher;

	@PostConstruct
	public void init() {
		this.pathMatcher = new OrRequestMatcher(
			config
				.getUnauthenticatedUrlPatterns()
				.stream()
				.map(p -> new AntPathRequestMatcher(p))
				.collect(Collectors.toList())
		);
	}

	@Override
	public boolean matches(HttpServletRequest request) {
		return pathMatcher.matches(request);
	}
}
