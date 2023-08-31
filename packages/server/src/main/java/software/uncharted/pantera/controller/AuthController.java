package software.uncharted.pantera.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.uncharted.pantera.service.CurrentUserService;

@RequestMapping("/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {

  private final CurrentUserService currentUserService;

  @GetMapping
  ResponseEntity<String> isAuthenticated() {
    final Jwt token = currentUserService.getToken();
    return ResponseEntity.ok(token.getTokenValue());
  }
}
