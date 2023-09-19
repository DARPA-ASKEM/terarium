package software.uncharted.terarium.hmiserver.resources.permissions;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import software.uncharted.terarium.hmiserver.models.permissions.PermissionGroup;
import software.uncharted.terarium.hmiserver.models.permissions.PermissionUser;
import software.uncharted.terarium.hmiserver.utils.rebac.ReBACService;
import software.uncharted.terarium.hmiserver.utils.rebac.askem.RebacGroup;
import software.uncharted.terarium.hmiserver.utils.rebac.askem.RebacProject;
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

	@POST
	@Tag(name = "Post group")
	public Response addGroup(
		@QueryParam("name") final String name
	) {
		try {
			RebacUser rebacUser = new RebacUser(jwt.getSubject(), reBACService);
			if (rebacUser.canAdministrate(new RebacGroup(reBACService.PUBLIC_GROUP_ID))) {
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
}
