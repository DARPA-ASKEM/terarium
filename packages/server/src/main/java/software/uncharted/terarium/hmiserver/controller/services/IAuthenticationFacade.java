package software.uncharted.terarium.hmiserver.controller.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

public interface IAuthenticationFacade {
    Authentication getAuthentication();
}
