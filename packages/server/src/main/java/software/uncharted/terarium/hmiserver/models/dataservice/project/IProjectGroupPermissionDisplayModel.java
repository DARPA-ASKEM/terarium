package software.uncharted.terarium.hmiserver.models.dataservice.project;

import software.uncharted.terarium.hmiserver.models.Group;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

public interface IProjectGroupPermissionDisplayModel {
	String getId();
	Group getGroup();
	String getName();
	Schema.Permission getPermissionLevel();
}
