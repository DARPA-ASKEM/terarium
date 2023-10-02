package software.uncharted.terarium.hmiserver.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/authorities")
public class AuthoritiesTestController {

	@GetMapping("/hasCreateUsers")
	@PreAuthorize("hasAuthority('CREATE_USERS')")
	public ResponseEntity<String> hasCreateUsers() {
		return ResponseEntity.ok("hasCreateUsers");
	}

	@GetMapping("/hasRoleAdmin")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<String> hasRoleAdmin() {
		return ResponseEntity.ok("hasRoleAdmin");
	}
}
