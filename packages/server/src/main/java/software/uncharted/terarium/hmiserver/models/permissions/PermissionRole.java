package software.uncharted.terarium.hmiserver.models.permissions;

import java.util.List;

public class PermissionRole {

	private String id;
	private String name;
	private List<PermissionUser> users;

	public PermissionRole(String id, String name, List<PermissionUser> users) {
		this.id = id;
		this.name = name;
		this.users = users;
	}

	public PermissionRole(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<PermissionUser> getUsers() {
		return users;
	}
}
