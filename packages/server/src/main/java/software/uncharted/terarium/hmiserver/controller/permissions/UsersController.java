package software.uncharted.terarium.hmiserver.controller.permissions;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.models.permissions.PermissionUser;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.utils.rebac.ReBACService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {
	@Autowired
	ReBACService reBACService;

	@GetMapping
	@Secured(Roles.USER)
	public ResponseEntity<List<PermissionUser>> getUsers(
		@RequestParam(name = "page_size", defaultValue = "1000") Integer pageSize,
		@RequestParam(name = "page", defaultValue = "0") Integer page
	) {
		return ResponseEntity.ok(reBACService.getUsers());
	}

	@DeleteMapping("/{userId}/roles/{roleName}")
	@Secured(Roles.ADMIN)
	public ResponseEntity<Void> deleteRoleFromUser(
		@PathVariable("userId") final String userId,
		@PathVariable("roleName") final String roleName
	) {
		if (roleName == null) {
			return ResponseEntity.badRequest().build();
		}

		try {
			return reBACService.deleteRoleFromUser(roleName, userId);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	@PostMapping("/{userId}/roles/{roleName}")
	@Secured(Roles.ADMIN)
	public ResponseEntity<Void> addRoleToUser(
		@PathVariable("userId") final String userId,
		@PathVariable("roleName") final String roleName
	) {
		if (roleName == null) {
			return ResponseEntity.badRequest().build();
		}

		try {
			return reBACService.addRoleToUser(roleName, userId);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

}
