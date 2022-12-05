package software.uncharted.terarium.hmiserver.models.dataservice;

import java.util.Arrays;

public enum Role {
	AUTHOR("author"),
	CONTRIBUTOR("contributor"),
	MAINTAINER("maintainer"),
	OTHER("other");

	public final String type;

	/**
	 * Returns the enum name for a given value of Role
	 *
	 * @param type the Role to attempt to find the name for.
	 * @return a String representation of the Role name
	 * @throws IllegalArgumentException if the Role is not found
	 */
	public static String findByType(final String type) {

		return Arrays.stream(values()).filter(
			value -> type.equalsIgnoreCase(value.type)).findFirst().orElseThrow(() -> new IllegalArgumentException("No Role with type: " + type)
		).name();

	}

	Role(final String type) {
		this.type = type;
	}
}
