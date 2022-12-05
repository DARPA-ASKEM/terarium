package software.uncharted.terarium.hmiserver.models.dataservice;

import java.util.Arrays;

public enum ValueType {
	BINARY("binary"),
	BOOL("bool"),
	FLOAT("float"),
	INT("int"),
	STR("str");

	public final String type;

	/**
	 * Returns the enum name for a given value of ResourceType
	 *
	 * @param type the ValueType to attempt to find the name for.
	 * @return a String representation of the ValueType name
	 * @throws IllegalArgumentException if the ValueType is not found
	 */
	public static String findByType(final String type) {

		return Arrays.stream(values()).filter(
			value -> type.equalsIgnoreCase(value.type)).findFirst().orElseThrow(() -> new IllegalArgumentException("No ValueType with type: " + type)
		).name();

	}

	ValueType(final String type) {
		this.type = type;
	}
}
