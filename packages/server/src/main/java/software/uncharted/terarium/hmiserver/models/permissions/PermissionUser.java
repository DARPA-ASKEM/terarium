package software.uncharted.terarium.hmiserver.models.permissions;

import java.util.List;

public class PermissionUser {
	private String id;
	private String firstName;
	private String lastName;
	private String email;
	private List<PermissionRole> roles;

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
}
