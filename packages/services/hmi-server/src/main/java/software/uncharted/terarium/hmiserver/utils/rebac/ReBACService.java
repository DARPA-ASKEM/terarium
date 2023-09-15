package software.uncharted.terarium.hmiserver.utils.rebac;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import com.authzed.api.v1.PermissionService.Consistency;
import com.authzed.grpcutil.BearerToken;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import software.uncharted.terarium.hmiserver.models.permissions.PermissionGroup;
import software.uncharted.terarium.hmiserver.models.permissions.PermissionUser;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ReBACService {
	@Inject
	Keycloak keycloak;

	@ConfigProperty(name = "admin.keycloak.realm")
	String REALM_NAME;
	@ConfigProperty(name = "admin.spicedb.shared-key")
	String SPICEDB_PRESHARED_KEY;
	@ConfigProperty(name = "admin.spicedb.target")
	String SPICEDB_TARGET;

	private BearerToken spiceDbBearerToken;
	private ManagedChannel channel;
	private SchemaManager schemaManager = new SchemaManager();

	public static final String PUBLIC_GROUP_NAME = "Public";
	public static String PUBLIC_GROUP_ID;
	public static final String ASKEM_ADMIN_GROUP_NAME = "ASKEM Admins";
	public static String ASKEM_ADMIN_GROUP_ID;

	public ReBACService() throws UnsupportedEncodingException {
	}

	void startup(@Observes StartupEvent event) throws Exception {
		spiceDbBearerToken = new BearerToken(SPICEDB_PRESHARED_KEY);
		channel = ManagedChannelBuilder
			.forTarget(SPICEDB_TARGET)
			//.useTransportSecurity() // for TLS communication
			.usePlaintext()
			.build();

		System.out.printf("Init ReBAC");
		if( !schemaManager.doesSchemaExist(channel, spiceDbBearerToken) ) {
			schemaManager.createSchema(channel, spiceDbBearerToken, Schema.schema);

			PUBLIC_GROUP_ID = createGroup(PUBLIC_GROUP_NAME);
			ASKEM_ADMIN_GROUP_ID = createGroup(ASKEM_ADMIN_GROUP_NAME);

			UsersResource usersResource = keycloak.realm(REALM_NAME).users();
			List<UserRepresentation> users = usersResource.list();
			for (UserRepresentation userRepresentation : users) {
				if (userRepresentation.getEmail() == null || userRepresentation.getEmail().isBlank()) { continue; }
				UserResource userResource = usersResource.get(userRepresentation.getId());
				String userId = userRepresentation.getId();
				SchemaObject user = new SchemaObject(Schema.Type.USER, userId);
				SchemaObject publicGroup = new SchemaObject(Schema.Type.GROUP, PUBLIC_GROUP_ID);
				SchemaObject adminGroup = new SchemaObject(Schema.Type.GROUP, ASKEM_ADMIN_GROUP_ID);

				for (RoleRepresentation roleRepresentation: userResource.roles().getAll().getRealmMappings()) {
					if (roleRepresentation.getDescription().isBlank()) {
						switch( roleRepresentation.getName() ) {
							case "user":
								createRelationship(user, publicGroup, Schema.Relationship.MEMBER);
								break;
							case "admin":
								createRelationship(user, publicGroup, Schema.Relationship.ADMIN);
								createRelationship(user, adminGroup, Schema.Relationship.ADMIN);
								break;
						}
					}
				}
			}
		} else {
			PUBLIC_GROUP_ID = getGroupId(PUBLIC_GROUP_NAME);
			ASKEM_ADMIN_GROUP_ID = getGroupId(ASKEM_ADMIN_GROUP_NAME);
		}
	}

	private String getGroupId(String name) {
		List<GroupRepresentation> groups = keycloak.realm(REALM_NAME).groups().groups(name, true, 0, Integer.MAX_VALUE, true);
		for (GroupRepresentation group : groups) {
			if( group.getName().equals(group.getPath()) ) {
				return group.getId();
			}
		}
		return null;
	}

	private Response createGroupMaybeParent(String parentId, GroupRepresentation group) {
		if (parentId == null) {
			return keycloak.realm(REALM_NAME).groups().add(group);
		} else {
			GroupResource parentGroup = keycloak.realm(REALM_NAME).groups().group(parentId);
			return parentGroup.subGroup(group);
		}
	}

	private String createGroup(String parentId, String name) {
		GroupRepresentation groupRepresentation = new GroupRepresentation();
		groupRepresentation.setName(name);

		Response response = createGroupMaybeParent(parentId, groupRepresentation);
		switch (response.getStatus()) {
			case 201:
				return CreatedResponseUtil.getCreatedId(response);
			case 409:
				System.err.println("Conflicting Name");
				return null;
			default:
				System.err.println("Other Error: " + response.getStatus());
				return null;
		}
	}

	private String createGroup(String name) {
		return this.createGroup(null, name);
	}

	public PermissionGroup addGroup(String name) {
		return new PermissionGroup(
			createGroup(name),
			name);
	}

	public List<PermissionUser> getUsers() {
		List<PermissionUser> response = new ArrayList<>();
		UsersResource usersResource = keycloak.realm(REALM_NAME).users();
		List<UserRepresentation> users = usersResource.list();
		for (UserRepresentation userRepresentation : users) {
			if (userRepresentation.getEmail() == null || userRepresentation.getEmail().isBlank()) {
				continue;
			}
			UserResource userResource = usersResource.get(userRepresentation.getId());
			boolean hasUserRole = false;
			for (RoleRepresentation roleRepresentation: userResource.roles().getAll().getRealmMappings()) {
				if (roleRepresentation.getDescription().isBlank()) {
					if (roleRepresentation.getName().equals("user")) {
						hasUserRole = true;
					}
				}
			}
			if (hasUserRole) {
				PermissionUser user = new PermissionUser(
					userRepresentation.getId(),
					userRepresentation.getFirstName(),
					userRepresentation.getLastName(),
					userRepresentation.getEmail());
				response.add(user);
			}
		}
		return response;
	}

	public List<PermissionGroup> getGroups() {
		List<PermissionGroup> response = new ArrayList<>();

		List<GroupRepresentation> groups = keycloak.realm(REALM_NAME).groups().groups();
		for (GroupRepresentation groupRepresentation : groups) {
			PermissionGroup group = new PermissionGroup(
				groupRepresentation.getId(),
				groupRepresentation.getName());
			response.add(group);
		}

		return response;
	}

	public boolean canRead(SchemaObject who, SchemaObject what) throws Exception {
		Consistency full = Consistency.newBuilder().setFullyConsistent(true).build();
		ReBACFunctions rebac = new ReBACFunctions(channel, spiceDbBearerToken);
		return rebac.checkPermission(who, Schema.Permission.READ, what, full);
	}

	public boolean canWrite(SchemaObject who, SchemaObject what) throws Exception {
		Consistency full = Consistency.newBuilder().setFullyConsistent(true).build();
		ReBACFunctions rebac = new ReBACFunctions(channel, spiceDbBearerToken);
		return rebac.checkPermission(who, Schema.Permission.WRITE, what, full);
	}

	public boolean canAdministrate(SchemaObject who, SchemaObject what) throws Exception {
		Consistency full = Consistency.newBuilder().setFullyConsistent(true).build();
		ReBACFunctions rebac = new ReBACFunctions(channel, spiceDbBearerToken);
		return rebac.checkPermission(who, Schema.Permission.ADMINISTRATE, what, full);
	}

	public void createRelationship(SchemaObject who, SchemaObject what, Schema.Relationship relationship) throws Exception {
		ReBACFunctions rebac = new ReBACFunctions(channel, spiceDbBearerToken);
		rebac.createRelationship(who, relationship, what);
	}

	public void getDatumDetails(String datumId, AskemDatumType datumType) throws Exception {
		SchemaObject datum = new SchemaObject(Schema.Type.DATUM, datumType + datumId);
	}
}
