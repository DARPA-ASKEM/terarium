package software.uncharted.terarium.hmiserver.controller.services;

import org.springframework.security.core.Authentication;

public interface IAuthenticationFacade {
    Authentication getAuthentication();
}
