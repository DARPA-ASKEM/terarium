package software.uncharted.terarium.hmiserver.models.authority;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Getter
@Slf4j
@TSModel
public enum AuthorityType {
	GRANT_AUTHORITY("Allows the user to grant authorities to other users"),
	USERS("Generic user authority");

	private final String description;

	AuthorityType(String description) {
		this.description = description;
	}

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
