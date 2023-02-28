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
	 * Returns the enum for a given string representation of a ValueType
	 *
	 * @param type the string representation of a ValueType
	 * @return a ValueType from the type string
	 * @throws IllegalArgumentException if the ValueType is not found
	 */
	public static ValueType findByType(final String type) {
		return Arrays.stream(values()).filter(
			value -> type.equalsIgnoreCase(value.type)).findFirst().orElseThrow(() -> new IllegalArgumentException("No ValueType with type: " + type)
		);
	}

	ValueType(final String type) {
		this.type = type;
	}
}
