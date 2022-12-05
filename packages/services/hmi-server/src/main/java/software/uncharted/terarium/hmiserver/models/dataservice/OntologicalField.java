package software.uncharted.terarium.hmiserver.models.dataservice;

import java.util.Arrays;

public enum OntologicalField {
	OBJECT("obj"),
	UNIT("unit");

	public final String type;

	/**
	 * Returns the enum name for a given value of OntologicalField
	 *
	 * @param type the OntologicalField to attempt to find the name for.
	 * @return a String representation of the OntologicalField name
	 * @throws IllegalArgumentException if the OntologicalField is not found
	 */
	public static String findByType(final String type) {

		return Arrays.stream(values()).filter(
			value -> type.equalsIgnoreCase(value.type)).findFirst().orElseThrow(() -> new IllegalArgumentException("No OntologicalField with type: " + type)
		).name();

	}

	OntologicalField(final String type) {
		this.type = type;
	}
}
