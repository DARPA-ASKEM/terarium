package software.uncharted.terarium.hmiserver.models.permissions;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@TSModel
@Data
@Accessors(chain = true)
public class PermissionProject {

	private String id;
	private String relationship;

	public PermissionProject(String id) {
		this.id = id;
	}
}
