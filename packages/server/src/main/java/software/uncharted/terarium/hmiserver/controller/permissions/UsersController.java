package software.uncharted.terarium.hmiserver.controller.permissions;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.models.authority.RoleType;
import software.uncharted.terarium.hmiserver.models.permissions.PermissionUser;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.utils.rebac.ReBACService;
import software.uncharted.terarium.hmiserver.utils.rebac.RelationsipAlreadyExistsException.RelationshipAlreadyExistsException;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;
import software.uncharted.terarium.hmiserver.utils.rebac.askem.RebacGroup;
import software.uncharted.terarium.hmiserver.utils.rebac.askem.RebacUser;

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
			if (roleName.equals(RoleType.ADMIN.name().toLowerCase())) {
				RebacGroup adminGroup = new RebacGroup(ReBACService.ASKEM_ADMIN_GROUP_ID, reBACService);
				RebacUser who = new RebacUser(userId, reBACService);
				adminGroup.removePermissionRelationships(who, Schema.Relationship.ADMIN.toString());
				RebacGroup publicGroup = new RebacGroup(ReBACService.PUBLIC_GROUP_ID, reBACService);
				publicGroup.removePermissionRelationships(who, Schema.Relationship.ADMIN.toString());
			}
			if (roleName.equals(RoleType.USER.name().toLowerCase())) {
				RebacGroup publicGroup = new RebacGroup(ReBACService.PUBLIC_GROUP_ID, reBACService);
				RebacUser who = new RebacUser(userId, reBACService);
				publicGroup.removePermissionRelationships(who, Schema.Relationship.MEMBER.toString());
			}
			return reBACService.deleteRoleFromUser(roleName, userId);
		} catch (Exception | RelationshipAlreadyExistsException e) {
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
			if (roleName.equals(RoleType.ADMIN.name().toLowerCase())) {
				RebacGroup adminGroup = new RebacGroup(ReBACService.ASKEM_ADMIN_GROUP_ID, reBACService);
				RebacUser who = new RebacUser(userId, reBACService);
				adminGroup.setPermissionRelationships(who, Schema.Relationship.ADMIN.toString());
				RebacGroup publicGroup = new RebacGroup(ReBACService.PUBLIC_GROUP_ID, reBACService);
				publicGroup.setPermissionRelationships(who, Schema.Relationship.ADMIN.toString());
			}
			if (roleName.equals(RoleType.USER.name().toLowerCase())) {
				RebacGroup publicGroup = new RebacGroup(ReBACService.PUBLIC_GROUP_ID, reBACService);
				RebacUser who = new RebacUser(userId, reBACService);
				publicGroup.setPermissionRelationships(who, Schema.Relationship.MEMBER.toString());
			}
			return reBACService.addRoleToUser(roleName, userId);
		} catch (Exception | RelationshipAlreadyExistsException e) {
			return ResponseEntity.internalServerError().build();
		}
	}

}
