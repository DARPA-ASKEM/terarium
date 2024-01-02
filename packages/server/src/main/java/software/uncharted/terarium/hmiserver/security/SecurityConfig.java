package software.uncharted.terarium.hmiserver.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.controller.internal.InternalController;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableGlobalMethodSecurity (
	securedEnabled = true
//	jsr250Enabled = true,
//	prePostEnabled = true
)
public class SecurityConfig {
	private final KeycloakLogoutHandler keycloakLogoutHandler;

	@Autowired
	private KeycloakJwtAuthenticationConverter authenticationConverter;

	@Autowired
	private Config config;

	SecurityConfig(KeycloakLogoutHandler keycloakLogoutHandler) {
		this.keycloakLogoutHandler = keycloakLogoutHandler;
	}

	@Bean
	protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
		return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
	}

	@Bean
//	@Order(1)
	public SecurityFilterChain initialSecurityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests((authorize) -> {
			if (config.getEnableSwagger()) {
				authorize.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**").permitAll();
			}
				authorize
				.requestMatchers(config.getUnauthenticatedUrlPatterns().toArray(new String[0])).permitAll()
				.requestMatchers(new AntPathRequestMatcher(InternalController.PATH+ "/**")).permitAll()
				.anyRequest().authenticated();
		});
		http.oauth2ResourceServer(configurer -> configurer.jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(authenticationConverter)));
		return http.build();
	}

//	@Bean
//	@Order(2)
//	public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
//		http
//			.securityMatcher("/api/users/**")
//			.authorizeHttpRequests((authorize) -> authorize
//				.anyRequest().hasRole(Roles.ADMIN)
//			);
//		http.oauth2ResourceServer(configurer -> configurer.jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(authenticationConverter)));
//		return http.build();
//	}
//
//	@Bean
//	@Order(2)
//	public SecurityFilterChain userSecurityFilterChain(HttpSecurity http) throws Exception {
//		http
//			.securityMatcher("/api/**")
//			.authorizeHttpRequests((authorize) -> authorize
//				.anyRequest().hasRole(Roles.USER)
//			);
//		http.oauth2ResourceServer(configurer -> configurer.jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(authenticationConverter)));
//		return http.build();
//	}
}
