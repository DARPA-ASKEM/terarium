package software.uncharted.terarium.hmiserver.models.dataservice.permission;

public class Group {
	private String id;
	private String relationship;

	public Group(String id, String relationship) {
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
