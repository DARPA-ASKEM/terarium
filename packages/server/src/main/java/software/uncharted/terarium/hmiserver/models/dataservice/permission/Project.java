package software.uncharted.terarium.hmiserver.models.dataservice.permission;

public class Project {
	private String id;
	private String relationship;

	public Project(String id, String relationship) {
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
