package software.uncharted.terarium.hmiserver.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import software.uncharted.terarium.hmiserver.models.User;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

	private final CurrentUserService currentUserService;

	@GetMapping("/me")
	public ResponseEntity<User> getWhoAmI() {
		return ResponseEntity.ok(currentUserService.get());
	}
}
