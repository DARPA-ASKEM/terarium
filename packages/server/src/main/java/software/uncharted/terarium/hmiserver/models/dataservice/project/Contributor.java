package software.uncharted.terarium.hmiserver.models.dataservice.project;

import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

/** A Contributor is a User or Group that is capable of editing a Project. */
public class Contributor {

	private final String userId;
	String name;
	Schema.Relationship permission;

	public Contributor(final String name, final String userId, final Schema.Relationship permission) {
		this.name = name;
		this.userId = userId;
		this.permission = permission;
	}

	public String getName() {
		return name;
	}

	public Schema.Relationship getPermission() {
		return permission;
	}

	public String getUserId() {
		return userId;
	}

	/** Is the Contributor a Group? */
	public Boolean isGroup() {
		return userId == null;
	}

	/** Is the Contributor a User? */
	public Boolean isUser() {
		return userId != null;
	}
}
