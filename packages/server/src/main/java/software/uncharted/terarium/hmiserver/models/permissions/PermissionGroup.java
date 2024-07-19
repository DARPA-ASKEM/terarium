package software.uncharted.terarium.hmiserver.models.permissions;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

@TSModel
@Data
@Accessors(chain = true)
public class PermissionGroup {

	private String id;
	private String name;

	@TSOptional
	private String relationship;

	@TSOptional
	private PermissionRelationships permissionRelationships;

	public PermissionGroup(String id, String name) {
		this.id = id;
		this.name = name;
	}
}
