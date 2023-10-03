package software.uncharted.terarium.hmiserver.controller.permissions;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.models.dataservice.permission.PermissionRelationships;
import software.uncharted.terarium.hmiserver.models.permissions.PermissionGroup;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.utils.rebac.ReBACService;
import software.uncharted.terarium.hmiserver.utils.rebac.RelationsipAlreadyExistsException.RelationshipAlreadyExistsException;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;
import software.uncharted.terarium.hmiserver.utils.rebac.askem.RebacGroup;
import software.uncharted.terarium.hmiserver.utils.rebac.askem.RebacPermissionRelationship;
import software.uncharted.terarium.hmiserver.utils.rebac.askem.RebacUser;

import java.util.List;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
@Slf4j
public class GroupsController {
	@Autowired
	ReBACService reBACService;

	private final CurrentUserService currentUserService;

	@GetMapping
	public ResponseEntity<List<PermissionGroup>> getGroups(
		@RequestParam(name = "page_size", defaultValue = "1000") Integer pageSize,
		@RequestParam(name = "page", defaultValue = "0") Integer page
	) {
		return ResponseEntity.ok(reBACService.getGroups());
	}

	@GetMapping("/{groupId}")
	public ResponseEntity<PermissionGroup> getGroup(
		@PathVariable("groupId") final String groupId
	) {
		try {
			RebacGroup rebacGroup = new RebacGroup(groupId, reBACService);
			PermissionGroup permissionGroup = reBACService.getGroup(groupId);
			if (new RebacUser(currentUserService.getToken().getSubject(), reBACService).canRead(rebacGroup)) {
				List<RebacPermissionRelationship> relationships = reBACService.getRelationships(rebacGroup.getSchemaObject());

				PermissionRelationships permissions = new PermissionRelationships();
				for (RebacPermissionRelationship permissionRelationship : relationships) {
					if (permissionRelationship.getSubjectType().equals(Schema.Type.USER)) {
						permissions.addUser(permissionRelationship.getSubjectId(), permissionRelationship.getRelationship());
					} else if (permissionRelationship.getSubjectType().equals(Schema.Type.GROUP)) {
						permissions.addGroup(permissionRelationship.getSubjectId(), permissionRelationship.getRelationship());
					}
				}

				return ResponseEntity.ok(permissionGroup);
			}
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			log.error("Error getting group", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@PostMapping
	public ResponseEntity<PermissionGroup> addGroup(
		@RequestParam(name = "name") final String name
	) {
		try {
			RebacUser rebacUser = new RebacUser(currentUserService.getToken().getSubject(), reBACService);
			if (rebacUser.canAdministrate(new RebacGroup(reBACService.PUBLIC_GROUP_ID, reBACService))) {
				PermissionGroup permissionGroup = rebacUser.addGroup(name);
				return ResponseEntity.ok(permissionGroup);
			}
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			log.error("Error adding group", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@PostMapping("/{groupId}/permissions/user/{userId}/{relationship}")
	public ResponseEntity<JsonNode> addGroupUserPermissions(
		@PathVariable("groupId") final String groupId,
		@PathVariable("userId") final String userId,
		@PathVariable("relationship") final String relationship
	) {
		try {
			RebacGroup what = new RebacGroup(userId, reBACService);
			RebacGroup who = new RebacGroup(groupId, reBACService);
			if (new RebacUser(currentUserService.getToken().getSubject(), reBACService).canAdministrate(what)) {
				what.setPermissionRelationships(who, relationship);
				return ResponseEntity.ok().build();
			}
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			log.error("Error adding group user permission relationships", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@DeleteMapping("/{groupId}/permissions/user/{userId}/{relationship}")
	public ResponseEntity<JsonNode> removeGroupUserPermissions(
		@PathVariable("groupId") final String groupdId,
		@PathVariable("userId") final String userId,
		@PathVariable("relationship") final String relationship
	) {
		try {
			RebacGroup what = new RebacGroup(groupdId, reBACService);
			RebacUser who = new RebacUser(userId, reBACService);
			if (new RebacUser(currentUserService.getToken().getSubject(), reBACService).canAdministrate(what)) {
				try {
					what.removePermissionRelationships(who, relationship);
					return ResponseEntity.ok().build();
				} catch (RelationshipAlreadyExistsException e) {
					return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
				}
			}
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			log.error("Error removing group user permission relationships", e);
			return ResponseEntity.internalServerError().build();
		}
	}
}
