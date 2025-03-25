package software.uncharted.terarium.hmiserver.models.dataservice.project;

import java.io.Serializable;
import java.util.Collection;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@TSModel
public class ProjectPermission implements Serializable {

	/** The id of the project this model is referring to */
	String projectId;

	/** A map from user uuid to username with permissions to this project */
	Collection<ProjectUserPermission> userPermissions;

	/** A map from group id to group name with permissions to read this project */
	Collection<ProjectGroupPermission> groupPermissions;
}
