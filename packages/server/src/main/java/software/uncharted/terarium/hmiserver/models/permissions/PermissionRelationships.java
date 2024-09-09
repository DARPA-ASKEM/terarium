package software.uncharted.terarium.hmiserver.models.permissions;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@TSModel
@Data
@Accessors(chain = true)
public class PermissionRelationships {

	private List<PermissionGroup> permissionGroups = new ArrayList<>();
	private List<PermissionUser> permissionUsers = new ArrayList<>();
	private List<PermissionProject> permissionProjects = new ArrayList<>();

	public void addUser(PermissionUser permissionUser, Schema.Relationship relationship) {
		permissionUser.setRelationship(relationship.toString());
		permissionUsers.add(permissionUser);
	}

	public void addGroup(PermissionGroup permissionGroup, Schema.Relationship relationship) {
		permissionGroup.setRelationship(relationship.toString());
		permissionGroups.add(permissionGroup);
	}

	public void addProject(PermissionProject permissionProject, Schema.Relationship relationship) {
		permissionProject.setRelationship(relationship.toString());
		permissionProjects.add(permissionProject);
	}
}
