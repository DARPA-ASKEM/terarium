package software.uncharted.terarium.hmiserver.models.permissions;

import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

@TSModel
public class PermissionGroup {
	private String id;
	private String name;
	@TSOptional
	private String relationship;

	public PermissionGroup(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getRelationship() {
		return relationship;
	}
}
