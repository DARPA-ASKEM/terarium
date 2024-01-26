package software.uncharted.terarium.hmiserver.filters;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.RequiredArgsConstructor;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.service.KeycloakTokenService;

@RequiredArgsConstructor
public class ServiceRequestFilter extends GenericFilterBean {
	private final KeycloakTokenService keycloakTokenService;
	private final Config config;

	public ServiceRequestFilter(ApplicationContext applicationContext) {
		this.keycloakTokenService = applicationContext.getBean(KeycloakTokenService.class);
		this.config = applicationContext.getBean(Config.class);
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		HeaderMapRequestWrapper wrappedRequest = new HeaderMapRequestWrapper((HttpServletRequest) servletRequest);

		// Get the authorization header from the wrappedRequest
		String authorizationHeader = wrappedRequest.getHeader("Authorization");

		// if the header is basic auth, then we need to use the token service to get the
		// jwt token
		if (authorizationHeader != null && authorizationHeader.startsWith("Basic")) {

			// Ensure that this requests path is a path that we want to use basic auth for
			String path = ((HttpServletRequest) servletRequest).getRequestURI();
			final AntPathMatcher matcher = new AntPathMatcher();
			boolean match = false;
			if (config.getServiceRequestPatterns() != null) {
				for (String pattern : config.getServiceRequestPatterns()) {
					if (matcher.match(pattern.trim(), path)) {
						match = true;
						break;
					}
				}
			}
			// If this does not match, the just pass the request through as we will return a
			// 401 later
			if (!match) {
				filterChain.doFilter(wrappedRequest, servletResponse);
				return;
			}

			// If we have a basic auth header, then we need to get the jwt token from the
			// token service
			final String basicAuth = authorizationHeader.substring("Basic".length()).trim();
			final String[] credentials = new String(Base64.getDecoder().decode(basicAuth)).split(":", 2);
			String jwtToken = keycloakTokenService.getToken(credentials[0], credentials[1]);

			// If this was successful and we have the correct roles, rewrite the auth token
			// to the bearer token and continue the filter chain
			if (jwtToken != null && keycloakTokenService.hasRealmRole(jwtToken, "service-account")) {
				wrappedRequest.addHeader("Authorization", "Bearer " + jwtToken);
			}
		}
		filterChain.doFilter(wrappedRequest, servletResponse);
	}

	static class HeaderMapRequestWrapper extends HttpServletRequestWrapper {
		public HeaderMapRequestWrapper(HttpServletRequest request) {
			super(request);
		}

		private final Map<String, String> headerMap = new HashMap<>();

		public void addHeader(String name, String value) {
			headerMap.put(name, value);
		}

		public void removeHeader(String name) {
			headerMap.remove(name);
		}

		@Override
		public String getHeader(String name) {
			String headerValue = super.getHeader(name);
			if (headerMap.containsKey(name)) {
				headerValue = headerMap.get(name);
			}
			return headerValue;
		}

		@Override
		public Enumeration<String> getHeaderNames() {
			List<String> names = Collections.list(super.getHeaderNames());
			names.addAll(headerMap.keySet());
			return Collections.enumeration(names);
		}

		@Override
		public Enumeration<String> getHeaders(String name) {
			List<String> values = Collections.list(super.getHeaders(name));
			if (headerMap.containsKey(name)) {
				values.add(headerMap.get(name));
			}
			return Collections.enumeration(values);
		}
	}
}
