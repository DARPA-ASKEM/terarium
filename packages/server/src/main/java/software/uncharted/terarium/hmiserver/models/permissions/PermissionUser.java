package software.uncharted.terarium.hmiserver.models.permissions;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

@TSModel
@Data
@Accessors(chain = true)
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
}
