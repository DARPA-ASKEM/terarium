package software.uncharted.terarium.hmiserver.models.dataservice.project;

import software.uncharted.terarium.hmiserver.models.User;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

public interface IProjectUserPermissionDisplayModel {
	String getId();
	User getUser();
	String getEmail();
	String getFamilyName();
	String getGivenName();
	String getUsername();

	Schema.Permission getPermissionLevel();
}
