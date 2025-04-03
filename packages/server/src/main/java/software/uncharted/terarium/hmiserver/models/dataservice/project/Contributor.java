package software.uncharted.terarium.hmiserver.models.dataservice.project;

import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

/** A Contributor is a User or Group that is capable of editing a Project. */
public class Contributor {

	private final String userId;
	private final String groupId;
	String name;
	Schema.Relationship permission;

	public Contributor(final String name, final String userId, final Schema.Relationship permission) {
		this.name = name;
		this.userId = userId;
		this.permission = permission;
		this.groupId = null;
	}

	public Contributor(
		final String name,
		final String userId,
		final String groupId,
		final Schema.Relationship permission
	) {
		this.name = name;
		this.userId = userId;
		this.permission = permission;
		this.groupId = groupId;
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

	public String getGroupId() {
		return groupId;
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
