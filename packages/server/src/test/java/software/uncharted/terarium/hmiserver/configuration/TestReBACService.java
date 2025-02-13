package software.uncharted.terarium.hmiserver.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.annotations.JsonResource;
import software.uncharted.terarium.hmiserver.models.authority.Role;
import software.uncharted.terarium.hmiserver.models.authority.User;
import software.uncharted.terarium.hmiserver.models.permissions.PermissionGroup;
import software.uncharted.terarium.hmiserver.models.permissions.PermissionRole;
import software.uncharted.terarium.hmiserver.models.permissions.PermissionUser;
import software.uncharted.terarium.hmiserver.utils.rebac.ReBACService;
import software.uncharted.terarium.hmiserver.utils.rebac.RelationsipAlreadyExistsException.RelationshipAlreadyExistsException;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;
import software.uncharted.terarium.hmiserver.utils.rebac.SchemaObject;
import software.uncharted.terarium.hmiserver.utils.rebac.askem.RebacPermissionRelationship;

@Service
@Primary
@Profile("test")
public class TestReBACService extends ReBACService {

	@JsonResource("classpath*:/mock-users/*.json")
	User[] USERS;

	private final Map<String, String> groups = new HashMap<>();
	private final Map<String, PermissionUser> users = new HashMap<>();
	private final Map<String, PermissionRole> roles = new HashMap<>();

	public TestReBACService(final Config config) {
		super(config);
	}

	@PostConstruct
	void init() {
		for (final User user : USERS) {
			final List<PermissionRole> roles = new ArrayList<>();
			for (final Role role : user.getRoles()) {
				final PermissionRole r = new PermissionRole(role.getId().toString(), role.getName());
				roles.add(r);
			}

			final PermissionUser permissionUser = new PermissionUser(
				user.getId(),
				user.getGivenName(),
				user.getFamilyName(),
				user.getEmail(),
				roles
			);

			users.put(user.getId(), permissionUser);
		}

		for (final User user : USERS) {
			final PermissionUser permissionUser = new PermissionUser(
				user.getId(),
				user.getGivenName(),
				user.getFamilyName(),
				user.getEmail()
			);

			for (final Role r : user.getRoles()) {
				PermissionRole role;
				if (roles.containsKey(r.getId().toString())) {
					role = roles.get(r.getId().toString());
				} else {
					role = new PermissionRole(r.getId().toString(), r.getName(), new ArrayList<>());
				}

				role.getUsers().add(permissionUser);

				roles.put(role.getId(), role);
			}
		}
	}

	@Override
	public PermissionGroup createGroup(final String name) {
		final String id = UUID.randomUUID().toString();
		groups.put(name, null);
		return new PermissionGroup(id, name);
	}

	@Override
	public PermissionUser getUser(final String id) {
		for (final User user : USERS) {
			if (user.getId() == id) {
				return new PermissionUser(id, user.getGivenName(), user.getFamilyName(), user.getEmail());
			}
		}
		throw new RuntimeException("User not found for id: " + id);
	}

	@Override
	public List<PermissionUser> getUsers() {
		return new ArrayList<>(users.values());
	}

	@Override
	public List<PermissionRole> getRoles() {
		return new ArrayList<>(roles.values());
	}

	@Override
	public List<PermissionGroup> getGroups() {
		final List<PermissionGroup> response = new ArrayList<>();
		for (final Map.Entry<String, String> group : groups.entrySet()) {
			final PermissionGroup permissionGroup = new PermissionGroup(group.getKey(), group.getValue());
			response.add(permissionGroup);
		}
		return response;
	}

	@Override
	public PermissionGroup getGroup(final String id) {
		if (groups.containsKey(id)) {
			return new PermissionGroup(id, groups.get(id));
		}
		throw new RuntimeException("Group not found for id: " + id);
	}

	public boolean canRead(final SchemaObject who, final SchemaObject what) throws Exception {
		return true;
	}

	public boolean canWrite(final SchemaObject who, final SchemaObject what) throws Exception {
		return true;
	}

	@Override
	public boolean isMemberOf(final SchemaObject who, final SchemaObject what) throws Exception {
		return true;
	}

	public boolean canAdministrate(final SchemaObject who, final SchemaObject what) throws Exception {
		return true;
	}

	@Override
	public boolean isCreator(final SchemaObject who, final SchemaObject what) throws Exception {
		return true;
	}

	@Override
	public void createRelationship(
		final SchemaObject who,
		final SchemaObject what,
		final Schema.Relationship relationship
	) throws Exception, RelationshipAlreadyExistsException {}

	@Override
	public void removeRelationship(
		final SchemaObject who,
		final SchemaObject what,
		final Schema.Relationship relationship
	) throws Exception {}

	@Override
	public List<RebacPermissionRelationship> getRelationships(final SchemaObject what) throws Exception {
		return new ArrayList<>();
	}

	@Override
	public ResponseEntity<Void> deleteRoleFromUser(final String roleName, final String userId) {
		if (!users.containsKey(userId)) {
			return ResponseEntity.notFound().build();
		}

		for (final Map.Entry<String, PermissionRole> entry : roles.entrySet()) {
			final PermissionRole role = entry.getValue();
			if (role.getName().equals(roleName)) {
				final boolean removed = role.getUsers().removeIf(user -> user.getId().equals(userId));
				if (removed) {
					return ResponseEntity.ok().build();
				}
			}
		}

		return ResponseEntity.notFound().build();
	}

	@Override
	public ResponseEntity<Void> addRoleToUser(final String roleName, final String userId) {
		if (!users.containsKey(userId)) {
			return ResponseEntity.notFound().build();
		}

		String roleId = null;
		PermissionRole role = null;

		for (final PermissionRole r : roles.values()) {
			if (r.getName().equals(roleName)) {
				roleId = r.getId();
				role = r;
			}
		}

		if (role == null) {
			roleId = UUID.randomUUID().toString();
			role = new PermissionRole(roleId, roleName);
			roles.put(role.getId(), role);
		}

		final PermissionUser user = users.get(userId);
		user.getRoles().add(new PermissionRole(roleId, roleName));

		role.getUsers().add(new PermissionUser(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail()));

		return ResponseEntity.ok().build();
	}

	@Override
	public List<UUID> lookupResources(final SchemaObject who, final Schema.Permission permission, final Schema.Type type)
		throws Exception {
		return new ArrayList<>();
	}
}
