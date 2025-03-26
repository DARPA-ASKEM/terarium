package software.uncharted.terarium.hmiserver.models.dataservice.project;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.User;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@Getter
@TSModel
@AllArgsConstructor
public class ProjectUserPermissionDisplayModel implements IProjectUserPermissionDisplayModel {

	private final String id;
	private final User user;
	private final String email;
	private final String familyName;
	private final String givenName;
	private final String username;
	private final Schema.Permission permissionLevel;

	@Setter
	private Schema.Permission inheritedPermissionLevel;

	public ProjectUserPermissionDisplayModel(IProjectUserPermissionDisplayModel model) {
		this.id = model.getId();
		this.user = model.getUser();
		this.email = model.getEmail();
		this.familyName = model.getFamilyName();
		this.givenName = model.getGivenName();
		this.username = model.getUsername();
		this.permissionLevel = model.getPermissionLevel();
	}

	@JsonProperty("permissionLevel")
	public Schema.Permission getProjectPermissionLevel() {
		return permissionLevel != null ? permissionLevel : Schema.Permission.NONE;
	}

	@JsonProperty("inheritedPermissionLevel")
	public Schema.Permission getInheritedProjectPermissionLevel() {
		return inheritedPermissionLevel != null ? inheritedPermissionLevel : Schema.Permission.NONE;
	}
}
