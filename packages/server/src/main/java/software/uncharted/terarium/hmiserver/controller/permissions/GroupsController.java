package software.uncharted.terarium.hmiserver.controller.permissions;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.models.permissions.PermissionGroup;
import software.uncharted.terarium.hmiserver.models.permissions.PermissionProject;
import software.uncharted.terarium.hmiserver.models.permissions.PermissionRelationships;
import software.uncharted.terarium.hmiserver.models.permissions.PermissionUser;
import software.uncharted.terarium.hmiserver.security.Roles;
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
	@Secured(Roles.USER)
	public ResponseEntity<List<PermissionGroup>> getGroups(
		@RequestParam(name = "page_size", defaultValue = "1000") Integer pageSize,
		@RequestParam(name = "page", defaultValue = "0") Integer page
	) {
		List<PermissionGroup> groups = reBACService.getGroups();
		return ResponseEntity.ok(groups);
	}

	@GetMapping("/{groupId}")
	@Secured({Roles.GROUP, Roles.ADMIN})
	public ResponseEntity<PermissionGroup> getGroup(
		@PathVariable("groupId") final String groupId
	) {
		try {
			RebacGroup rebacGroup = new RebacGroup(groupId, reBACService);
			if (new RebacUser(currentUserService.getToken().getSubject(), reBACService).isMemberOf(rebacGroup)) {
				List<RebacPermissionRelationship> relationships = reBACService.getRelationships(rebacGroup.getSchemaObject());
				PermissionRelationships permissions = new PermissionRelationships();
				for (RebacPermissionRelationship permissionRelationship : relationships) {
					if (permissionRelationship.getSubjectType().equals(Schema.Type.USER)) {
						PermissionUser user = reBACService.getUser(permissionRelationship.getSubjectId());
						permissions.addUser(user, permissionRelationship.getRelationship());
					} else if (permissionRelationship.getSubjectType().equals(Schema.Type.GROUP)) {
						PermissionGroup group = reBACService.getGroup(permissionRelationship.getSubjectId());
						permissions.addGroup(group, permissionRelationship.getRelationship());
					} else if (permissionRelationship.getSubjectType().equals(Schema.Type.PROJECT)) {
						PermissionProject project = new PermissionProject(permissionRelationship.getSubjectId());
						permissions.addProject(project, permissionRelationship.getRelationship());
					}
				}

				PermissionGroup permissionGroup = reBACService.getGroup(groupId);
				permissionGroup.setPermissionRelationships(permissions);
				return ResponseEntity.ok(permissionGroup);
			}
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			log.error("Error getting group", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@PostMapping
	@Secured({Roles.GROUP, Roles.ADMIN})
	public ResponseEntity<PermissionGroup> createGroup(
		@RequestParam(name = "name") final String name
	) {
		try {
			RebacUser rebacUser = new RebacUser(currentUserService.getToken().getSubject(), reBACService);
			try {
				PermissionGroup permissionGroup = rebacUser.createGroup(name);
				return ResponseEntity.ok(permissionGroup);
			} catch (RelationshipAlreadyExistsException e) {
				return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
			}
		} catch (Exception e) {
			log.error("Error adding group", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@PostMapping("/{groupId}/permissions/user/{userId}/{relationship}")
	@Secured({Roles.GROUP, Roles.ADMIN})
	public ResponseEntity<JsonNode> addGroupUserPermissions(
		@PathVariable("groupId") final String groupId,
		@PathVariable("userId") final String userId,
		@PathVariable("relationship") final String relationship
	) {
		try {
			RebacGroup what = new RebacGroup(groupId, reBACService);
			RebacUser who = new RebacUser(userId, reBACService);
			if (new RebacUser(currentUserService.getToken().getSubject(), reBACService).canAdministrate(what)) {
				try {
					what.setPermissionRelationships(who, relationship);
					return ResponseEntity.ok().build();
				} catch (RelationshipAlreadyExistsException e) {
					return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
				}
			}
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		} catch (Exception e) {
			log.error("Error adding group user permission relationships", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@PutMapping("/{groupId}/permissions/user/{userId}/{oldRelationship}")
	@Secured({Roles.GROUP, Roles.ADMIN})
	public ResponseEntity<JsonNode> updateGroupUserPermissions(
		@PathVariable("groupId") final String groupId,
		@PathVariable("userId") final String userId,
		@PathVariable("oldRelationship") final String oldRelationship,
		@RequestParam("to") final String newRelationship
	) {
		try {
			if (oldRelationship.equals(newRelationship)) {
				return ResponseEntity.badRequest().build();
			}
			RebacGroup what = new RebacGroup(groupId, reBACService);
			RebacUser who = new RebacUser(userId, reBACService);
			if (new RebacUser(currentUserService.getToken().getSubject(), reBACService).canAdministrate(what)) {
				try {
					 what.removePermissionRelationships(who, oldRelationship);
					 what.setPermissionRelationships(who, newRelationship);
					 return ResponseEntity.ok().build();
				} catch (RelationshipAlreadyExistsException e) {
					return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
				}
			}
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		} catch (Exception e) {
			log.error("Error adding group user permission relationships", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@DeleteMapping("/{groupId}/permissions/user/{userId}/{relationship}")
	@Secured({Roles.GROUP, Roles.ADMIN})
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
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		} catch (Exception e) {
			log.error("Error removing group user permission relationships", e);
			return ResponseEntity.internalServerError().build();
		}
	}
}
