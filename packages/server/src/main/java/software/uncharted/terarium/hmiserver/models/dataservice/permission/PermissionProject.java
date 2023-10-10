package software.uncharted.terarium.hmiserver.models.dataservice.permission;

public class PermissionProject {
	private String id;
	private String relationship;

	public PermissionProject(String id, String relationship) {
		this.id = id;
		this.relationship = relationship;
	}

	public String getId() {
		return id;
	}

	public String getRelationship() {
		return relationship;
	}

}
