package software.uncharted.terarium.hmiserver.models.permissions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@TSModel
@Data
@Accessors(chain = true)
@AllArgsConstructor
public class PermissionProject {
	private String id;
	private String relationship;

}
