package software.uncharted.terarium.hmiserver.models.permissions;

public class PermissionGroup {
	private String id;
	private String name;

	public PermissionGroup(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
