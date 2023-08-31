package software.uncharted.pantera.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import software.uncharted.pantera.configuration.Config;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests((authorize) -> authorize
      .requestMatchers(config.getUnauthenticatedUrlPatterns().toArray(new String[0])).permitAll()
      .anyRequest().authenticated());
    http.oauth2ResourceServer(configurer -> configurer.jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(authenticationConverter)));
    return http.build();
  }
}
