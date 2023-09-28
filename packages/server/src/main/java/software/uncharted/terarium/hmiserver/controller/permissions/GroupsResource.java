package software.uncharted.terarium.hmiserver.controller.permissions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.uncharted.terarium.hmiserver.models.dataservice.permission.PermissionRelationships;
import software.uncharted.terarium.hmiserver.models.permissions.PermissionGroup;
import software.uncharted.terarium.hmiserver.utils.rebac.ReBACService;
import software.uncharted.terarium.hmiserver.utils.rebac.RelationsipAlreadyExistsException.RelationshipAlreadyExistsException;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;
import software.uncharted.terarium.hmiserver.utils.rebac.askem.RebacGroup;
import software.uncharted.terarium.hmiserver.utils.rebac.askem.RebacPermissionRelationship;
import software.uncharted.terarium.hmiserver.utils.rebac.askem.RebacUser;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@RequestMapping("/groups")
@RestController
@Slf4j
@RequiredArgsConstructor
public class GroupsResource {
	@Inject
	ReBACService reBACService;

	JsonWebToken jwt;

	@GET
	public List<PermissionGroup> getGroups(
		@DefaultValue("1000") @QueryParam("page_size") final Integer pageSize,
		@DefaultValue("0") @QueryParam("page") final Integer page
	) {
		return reBACService.getGroups();
	}

	@GET
	@Path("/{groupId}")
	public Response getGroup(
		@PathParam("groupId") final String groupId
	) {
		try {
			RebacGroup rebacGroup = new RebacGroup(groupId, reBACService);
			PermissionGroup permissionGroup = reBACService.getGroup(groupId);
			if (new RebacUser(jwt.getSubject(), reBACService).canRead(rebacGroup)) {
				List<RebacPermissionRelationship> relationships = reBACService.getRelationships(rebacGroup.getSchemaObject());

				PermissionRelationships permissions = new PermissionRelationships();
				for (RebacPermissionRelationship permissionRelationship : relationships) {
					if (permissionRelationship.getSubjectType().equals(Schema.Type.USER)) {
						permissions.addUser(permissionRelationship.getSubjectId(), permissionRelationship.getRelationship());
					} else if (permissionRelationship.getSubjectType().equals(Schema.Type.GROUP)) {
						permissions.addGroup(permissionRelationship.getSubjectId(), permissionRelationship.getRelationship());
					}
				}

				return Response
					.status(Response.Status.OK)
					.entity(permissionGroup)
					.build();
			}
			return Response.status(Response.Status.NOT_FOUND).build();
		} catch (Exception e) {
			log.error("Error getting group", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	public Response addGroup(
		@QueryParam("name") final String name
	) {
		try {
			RebacUser rebacUser = new RebacUser(jwt.getSubject(), reBACService);
			if (rebacUser.canAdministrate(new RebacGroup(reBACService.PUBLIC_GROUP_ID, reBACService))) {
				PermissionGroup permissionGroup = rebacUser.addGroup(name);
				return Response
					.status(Response.Status.OK)
					.entity(permissionGroup)
					.build();
			}
			return Response.status(Response.Status.NOT_FOUND).build();
		} catch (Exception e) {
			log.error("Error adding group", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Path("/{groupId}/permissions/user/{userId}/{relationship}")
	public Response addGroupUserPermissions(
		@PathParam("groupId") final String groupId,
		@PathParam("userId") final String userId,
		@PathParam("relationship") final String relationship
	) {
		try {
			RebacGroup what = new RebacGroup(userId, reBACService);
			RebacGroup who = new RebacGroup(groupId, reBACService);
			if (new RebacUser(jwt.getSubject(), reBACService).canAdministrate(what)) {
				what.setPermissionRelationships(who, relationship);
				return Response.ok().build();
			}
			return Response.status(Response.Status.NOT_FOUND).build();
		} catch (Exception e) {
			log.error("Error adding group user permission relationships", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DELETE
	@Path("/{groupId}/permissions/user/{userId}/{relationship}")
	public Response removeGroupUserPermissions(
		@PathParam("groupId") final String groupdId,
		@PathParam("userId") final String userId,
		@PathParam("relationship") final String relationship
	) {
		try {
			RebacGroup what = new RebacGroup(groupdId, reBACService);
			RebacUser who = new RebacUser(userId, reBACService);
			if (new RebacUser(jwt.getSubject(), reBACService).canAdministrate(what)) {
				try {
					what.removePermissionRelationships(who, relationship);
					return Response.ok().build();
				} catch (RelationshipAlreadyExistsException e) {
					return Response.notModified().build();
				}
			}
			return Response.status(Response.Status.NOT_FOUND).build();
		} catch (Exception e) {
			log.error("Error removing group user permission relationships", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}
