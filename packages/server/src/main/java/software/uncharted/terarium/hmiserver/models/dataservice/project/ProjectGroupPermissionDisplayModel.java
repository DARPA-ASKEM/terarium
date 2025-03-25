package software.uncharted.terarium.hmiserver.models.dataservice.project;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import software.uncharted.terarium.hmiserver.models.Group;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@Getter
@AllArgsConstructor
public class ProjectGroupPermissionDisplayModel implements IProjectGroupPermissionDisplayModel {

	private final String id;
	private final Group group;
	private final String name;
	private final Schema.Permission permissionLevel;

	public ProjectGroupPermissionDisplayModel(IProjectGroupPermissionDisplayModel model) {
		this.id = model.getId();
		this.group = model.getGroup();
		this.name = model.getName();
		this.permissionLevel = model.getPermissionLevel();
	}

	@JsonProperty("permissionLevel")
	public Schema.Permission getProjectPermissionLevel() {
		return permissionLevel != null ? permissionLevel : Schema.Permission.NONE;
	}
}
