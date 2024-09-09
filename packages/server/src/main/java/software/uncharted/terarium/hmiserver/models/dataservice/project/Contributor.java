package software.uncharted.terarium.hmiserver.models.dataservice.project;

import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

/** A Contributor is a User or Group that is capable of editing a Project. */
public class Contributor {

	String name;
	Schema.Relationship permission;

	public Contributor(final String name, final Schema.Relationship permission) {
		this.name = name;
		this.permission = permission;
	}

	public String getName() {
		return name;
	}

	public Schema.Relationship getPermission() {
		return permission;
	}
}
