package software.uncharted.terarium.hmiserver.models.permissions;

import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@TSModel
public class PermissionUser {
	private String id;
	private String firstName;
	private String lastName;
	private String email;

	public PermissionUser(String id, String firstName, String lastName, String email) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
	}

	public String getId() { return id; }
	public String getFirstName() { return firstName; }
	public String getLastName() { return lastName; }
	public String getEmail() { return email; }
}
