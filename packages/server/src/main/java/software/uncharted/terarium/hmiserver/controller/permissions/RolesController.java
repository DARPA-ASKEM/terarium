package software.uncharted.terarium.hmiserver.controller.permissions;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.uncharted.terarium.hmiserver.models.permissions.PermissionRole;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.utils.rebac.ReBACService;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RolesController {

	private final ReBACService reBACService;

	@GetMapping
	@Secured(Roles.USER)
	public ResponseEntity<List<PermissionRole>> getRoles(
		@RequestParam(name = "page_size", defaultValue = "1000") final Integer pageSize,
		@RequestParam(name = "page", defaultValue = "0") final Integer page
	) {
		return ResponseEntity.ok(reBACService.getRoles());
	}
}
