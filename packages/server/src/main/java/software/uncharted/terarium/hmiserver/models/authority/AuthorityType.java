package software.uncharted.terarium.hmiserver.models.authority;

import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Slf4j
@TSModel
public enum AuthorityType {
	GRANT_AUTHORITY,
	USERS;

	public static AuthorityType get(String type) {
		try {
			return valueOf(type);
		} catch (IllegalArgumentException | NullPointerException e) {
			// type does not exist
			log.error("AuthorityType {} not found", type);
			return null;
		}
	}
}
