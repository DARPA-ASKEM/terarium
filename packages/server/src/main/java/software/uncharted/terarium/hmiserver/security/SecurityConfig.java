package software.uncharted.terarium.hmiserver.security;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import lombok.RequiredArgsConstructor;
import software.uncharted.terarium.hmiserver.filters.ServiceRequestFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(securedEnabled = true
// jsr250Enabled = true,
// prePostEnabled = true
)
public class SecurityConfig {

	private final KeycloakLogoutHandler keycloakLogoutHandler;
	private final KeycloakJwtAuthenticationConverter authenticationConverter;
	private final SwaggerRequestMatcher swaggerRequestMatcher;
	private final UnauthenticatedUrlRequestMatcher unauthenticatedUrlRequestMatcher;
	private final ApplicationContext applicationContext;

	@Bean
	protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
		return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
	}

	@Bean
	public SecurityFilterChain initialSecurityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests((authorize) -> {
			authorize
					.requestMatchers(swaggerRequestMatcher).permitAll()
					.requestMatchers(unauthenticatedUrlRequestMatcher).permitAll()
					.anyRequest().authenticated();
		});
		http.oauth2ResourceServer(configurer -> configurer
				.jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(authenticationConverter)));
		http.addFilterBefore(new ServiceRequestFilter(applicationContext),
				AbstractPreAuthenticatedProcessingFilter.class);
		return http.build();
	}

}
