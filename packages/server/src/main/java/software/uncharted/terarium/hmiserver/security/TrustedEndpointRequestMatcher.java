package software.uncharted.terarium.hmiserver.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.controller.TrustedController;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrustedEndpointRequestMatcher implements RequestMatcher {

	final private Config config;

	// final private List<TrustedController> trustedControllers;
	final private RequestMappingHandlerMapping requestMappingHandlerMapping;

	private RequestMatcher pathMatcher;

	@PostConstruct
	public void init() {

		List<String> trustedEndpoints = getTrustedControllerMappings();

		log.info("Trusted controller endpoints:" + trustedEndpoints);

		List<RequestMatcher> matchers = getTrustedControllerMappings().stream()
				.map(p -> new AntPathRequestMatcher("/**" + p + "/**"))
				.collect(Collectors.toList());
		this.pathMatcher = new OrRequestMatcher(matchers);
	}

	private List<String> getTrustedControllerMappings() {
		// This iterates over every controller that implements `TrustedController` and
		// returns all the top level @RequestMapping("path") values

		Set<String> paths = requestMappingHandlerMapping.getHandlerMethods().values().stream()
				.filter(handlerMethod -> TrustedController.class.isAssignableFrom(handlerMethod.getBeanType()))
				.map(handlerMethod -> {
					RequestMapping classRequestMappingAnnotation = AnnotationUtils
							.findAnnotation(handlerMethod.getBeanType(), RequestMapping.class);
					return classRequestMappingAnnotation != null ? classRequestMappingAnnotation.path()
							: new String[] {};
				})
				.flatMap(Arrays::stream)
				.collect(Collectors.toSet());

		return new ArrayList<>(paths);
	}

	@Override
	public boolean matches(HttpServletRequest request) {
		log.info("CHECKING: " + request.getLocalPort() + " against " + config.getTrustedPort() + ": "
				+ (request.getLocalPort() == config.getTrustedPort()));
		log.info("CHECKING pathMatcher.matches(request): " + pathMatcher.matches(request));
		log.info("FINAL MATCH RESULT IS: "
				+ (request.getLocalPort() == config.getTrustedPort() && pathMatcher.matches(request)));

		return request.getLocalPort() == config.getTrustedPort() && pathMatcher.matches(request);
	}
}
