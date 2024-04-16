package software.uncharted.terarium.hmiserver.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.annotations.JsonResource;
import software.uncharted.terarium.hmiserver.models.User;
import software.uncharted.terarium.hmiserver.models.authority.Role;
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
public class TestReBACService extends ReBACService {

    @JsonResource("classpath*:/mock-users/*.json")
    User[] USERS;

    private Map<String, String> groups = new HashMap<>();
    private Map<String, PermissionUser> users = new HashMap<>();
    private Map<String, PermissionRole> roles = new HashMap<>();

    public TestReBACService(final Config config) {
        super(config);
    }

    @PostConstruct
    void init() {

        for (User user : USERS) {
            List<PermissionRole> roles = new ArrayList<>();
            for (Role role : user.getRoles()) {
                PermissionRole r = new PermissionRole(role.getId().toString(), role.getName());
                roles.add(r);
            }

            PermissionUser permissionUser =
                    new PermissionUser(user.getId(), user.getGivenName(), user.getFamilyName(), user.getEmail(), roles);

            users.put(user.getId(), permissionUser);
        }

        for (User user : USERS) {
            PermissionUser permissionUser =
                    new PermissionUser(user.getId(), user.getGivenName(), user.getFamilyName(), user.getEmail());

            for (Role r : user.getRoles()) {

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

    public PermissionGroup createGroup(String name) {
        String id = UUID.randomUUID().toString();
        groups.put(name, null);
        return new PermissionGroup(id, name);
    }

    public PermissionUser getUser(String id) {
        for (User user : USERS) {
            if (user.getId() == id) {
                return new PermissionUser(id, user.getGivenName(), user.getFamilyName(), user.getEmail());
            }
        }
        throw new RuntimeException("User not found for id: " + id);
    }

    public List<PermissionUser> getUsers() {
        return new ArrayList<>(users.values());
    }

    public List<PermissionRole> getRoles() {
        return new ArrayList<>(roles.values());
    }

    public List<PermissionGroup> getGroups() {
        List<PermissionGroup> response = new ArrayList<>();
        for (Map.Entry<String, String> group : groups.entrySet()) {
            PermissionGroup permissionGroup = new PermissionGroup(group.getKey(), group.getValue());
            response.add(permissionGroup);
        }
        return response;
    }

    public PermissionGroup getGroup(String id) {
        if (groups.containsKey(id)) {
            return new PermissionGroup(id, groups.get(id));
        }
        throw new RuntimeException("Group not found for id: " + id);
    }

    public boolean canRead(SchemaObject who, SchemaObject what) throws Exception {
        return true;
    }

    public boolean canWrite(SchemaObject who, SchemaObject what) throws Exception {
        return true;
    }

    public boolean isMemberOf(SchemaObject who, SchemaObject what) throws Exception {
        return true;
    }

    public boolean canAdministrate(SchemaObject who, SchemaObject what) throws Exception {
        return true;
    }

    public boolean isCreator(SchemaObject who, SchemaObject what) throws Exception {
        return true;
    }

    public void createRelationship(SchemaObject who, SchemaObject what, Schema.Relationship relationship)
            throws Exception, RelationshipAlreadyExistsException {}

    public void removeRelationship(SchemaObject who, SchemaObject what, Schema.Relationship relationship)
            throws Exception, RelationshipAlreadyExistsException {}

    public List<RebacPermissionRelationship> getRelationships(SchemaObject what) throws Exception {
        return new ArrayList<>();
    }

    public ResponseEntity<Void> deleteRoleFromUser(String roleName, String userId) {

        if (!users.containsKey(userId)) {
            return ResponseEntity.notFound().build();
        }

        for (Map.Entry<String, PermissionRole> entry : roles.entrySet()) {
            PermissionRole role = entry.getValue();
            if (role.getName().equals(roleName)) {
                boolean removed = role.getUsers().removeIf(user -> user.getId().equals(userId));
                if (removed) {
                    return ResponseEntity.ok().build();
                }
            }
        }

        return ResponseEntity.notFound().build();
    }

    public ResponseEntity<Void> addRoleToUser(String roleName, String userId) {

        if (!users.containsKey(userId)) {
            return ResponseEntity.notFound().build();
        }

        String roleId = null;
        PermissionRole role = null;

        for (PermissionRole r : roles.values()) {
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

        PermissionUser user = users.get(userId);
        user.getRoles().add(new PermissionRole(roleId, roleName));

        role.getUsers().add(new PermissionUser(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail()));

        return ResponseEntity.ok().build();
    }

    public List<UUID> lookupResources(SchemaObject who, Schema.Permission permission, Schema.Type type)
            throws Exception {

        return new ArrayList<>();
    }
}
