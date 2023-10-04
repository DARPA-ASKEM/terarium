package software.uncharted.terarium.hmiserver.models.dataservice;

import java.util.Arrays;

public enum Role {
	AUTHOR("author"),
	CONTRIBUTOR("contributor"),
	MAINTAINER("maintainer"),
	OTHER("other");

	public final String type;

	/**
	 * Returns the enum for a given string representation of a Role
	 *
	 * @param type the string representation of a Role
	 * @return a Role from the type string
	 * @throws IllegalArgumentException if the Role is not found
	 */
	public static Role findByType(final String type) {
		return Arrays.stream(values()).filter(
			value -> type.equalsIgnoreCase(value.type)).findFirst().orElseThrow(() -> new IllegalArgumentException("No Role with type: " + type)
		);
	}

	Role(final String type) {
		this.type = type;
	}
}
