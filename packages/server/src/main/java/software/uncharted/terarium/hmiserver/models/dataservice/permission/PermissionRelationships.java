package software.uncharted.terarium.hmiserver.models.dataservice.permission;

import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

import java.util.ArrayList;
import java.util.List;

public class PermissionRelationships {
	private List<PermissionGroup> permissionGroups = new ArrayList<>();
	private List<PermissionUser> permissionUsers = new ArrayList<>();
	private List<PermissionProject> permissionProjects = new ArrayList<>();

	public void addUser(String id, Schema.Relationship relationship) {
		permissionUsers.add(new PermissionUser(id, relationship.toString()));
	}

	public void addGroup(String id, Schema.Relationship relationship) {
		permissionGroups.add(new PermissionGroup(id, relationship.toString()));
	}

	public void addProject(String id, Schema.Relationship relationship) {
		permissionProjects.add(new PermissionProject(id, relationship.toString()));
	}

	public List<PermissionGroup> getGroups() {
		return permissionGroups;
	}

	public List<PermissionUser> getUsers() {
		return permissionUsers;
	}

	public List<PermissionProject> getProjects() {
		return permissionProjects;
	}
}
