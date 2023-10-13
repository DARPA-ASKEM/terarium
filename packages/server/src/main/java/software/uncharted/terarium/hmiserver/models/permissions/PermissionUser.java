package software.uncharted.terarium.hmiserver.models.permissions;

import java.util.List;

import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

@TSModel
public class PermissionUser {
	private String id;
	private String firstName;
	private String lastName;
	private String email;
	@TSOptional
	private List<PermissionRole> roles;
	@TSOptional
	private String relationship;

	public PermissionUser(String id, String firstName, String lastName, String email, List<PermissionRole> roles) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.roles = roles;
	}

	public PermissionUser(String id, String firstName, String lastName, String email) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
	}

	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}

	public String getId() {
		return id;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getEmail() {
		return email;
	}

	public List<PermissionRole> getRoles() { return roles; }

	public String getRelationship() {
		return relationship;
	}
}
