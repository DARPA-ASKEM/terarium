package software.uncharted.terarium.hmiserver.models.authority;

import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

@Slf4j
@TSModel
public enum AuthorityType {
  GRANT_AUTHORITY("Allows the user to grant authorities to other users"),
  USERS("Generic user authority");

  @TSOptional
  private final String description;

  AuthorityType(final String description) {
    this.description = description;
  }

	public static AuthorityType get(final String type) {
		try {
			return valueOf(type);
		} catch (final IllegalArgumentException | NullPointerException e) {
			// type does not exist
			log.error("AuthorityType {} not found", type);
			return null;
		}
	}
}
