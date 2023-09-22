package software.uncharted.terarium.hmiserver.resources.permissions;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
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

@Path("/api/groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Groups REST Endpoints")
@Slf4j
public class GroupsResource {
	@Inject
	ReBACService reBACService;

	@Inject
	JsonWebToken jwt;

	@GET
	@Tag(name = "Get groups")
	public List<PermissionGroup> getGroups(
		@DefaultValue("1000") @QueryParam("page_size") final Integer pageSize,
		@DefaultValue("0") @QueryParam("page") final Integer page
	) {
		return reBACService.getGroups();
	}

	@GET
	@Tag(name = "Get group")
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
			return Response.status(404).build();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@POST
	@Tag(name = "Post group")
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
			return Response.status(404).build();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@POST
	@Tag(name = "Add user to group")
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
			return Response.status(404).build();
		} catch (Exception e) {
			throw new RuntimeException(e);
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
			return Response.status(404).build();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
