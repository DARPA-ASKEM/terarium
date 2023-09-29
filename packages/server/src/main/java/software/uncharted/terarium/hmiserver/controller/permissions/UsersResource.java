package software.uncharted.terarium.hmiserver.controller.permissions;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.uncharted.terarium.hmiserver.models.permissions.PermissionUser;
import software.uncharted.terarium.hmiserver.utils.rebac.ReBACService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersResource {
	@Autowired
	ReBACService reBACService;

	@GetMapping
	public ResponseEntity<List<PermissionUser>> getUsers(
		@RequestParam(name = "page_size", defaultValue = "1000") Integer pageSize,
		@RequestParam(name = "page", defaultValue = "0") Integer page
	) {
		return ResponseEntity.ok(reBACService.getUsers());
	}
}
