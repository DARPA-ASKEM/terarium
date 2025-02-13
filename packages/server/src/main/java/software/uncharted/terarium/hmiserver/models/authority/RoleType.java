package software.uncharted.terarium.hmiserver.models.authority;

import software.uncharted.terarium.hmiserver.annotations.TSModel;

@TSModel
public enum RoleType {
	ADMIN,
	USER,
	GROUP,
	SERVICE,
	TEST
}
