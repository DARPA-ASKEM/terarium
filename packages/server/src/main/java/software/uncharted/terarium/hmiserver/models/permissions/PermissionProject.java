package software.uncharted.terarium.hmiserver.models.permissions;

import software.uncharted.terarium.hmiserver.annotations.TSModel;

@TSModel
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
