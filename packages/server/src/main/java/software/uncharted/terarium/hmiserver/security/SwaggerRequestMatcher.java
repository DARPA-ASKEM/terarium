package software.uncharted.terarium.hmiserver.security;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.configuration.Config;

@Slf4j
@Component
@RequiredArgsConstructor
public class SwaggerRequestMatcher implements RequestMatcher {

	final private Config config;

	private RequestMatcher pathMatcher;

	final private List<String> SWAGGER_URLs = Arrays.asList(
			"/swagger-ui/**",
			"/swagger-ui.html",
			"/v3/api-docs/**",
			"/swagger-resources/**");

	@PostConstruct
	public void init() {
		if (config.getEnableSwagger()) {

			this.pathMatcher = new OrRequestMatcher(
					SWAGGER_URLs.stream()
							.map(p -> new AntPathRequestMatcher(p))
							.collect(Collectors.toList()));
		} else {
			// will become a no-op matcher that doesn't match anything
			this.pathMatcher = new NegatedRequestMatcher(AnyRequestMatcher.INSTANCE);
		}

	}

	@Override
	public boolean matches(HttpServletRequest request) {
		return pathMatcher.matches(request);
	}
}
