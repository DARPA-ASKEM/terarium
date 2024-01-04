package software.uncharted.terarium.hmiserver.models.dataservice;

import java.util.Arrays;

public enum OntologicalField {
	OBJECT("obj"),
	UNIT("unit");

	public final String type;

	/**
	 * Returns the enum for a given string representation of a OntologicalField
	 *
	 * @param type the string representation of a OntologicalField
	 * @return a OntologicalField from the type string
	 * @throws IllegalArgumentException if the OntologicalField is not found
	 */
	public static OntologicalField findByType(final String type) {
		return Arrays.stream(values()).filter(
			value -> type.equalsIgnoreCase(value.type)).findFirst().orElseThrow(() -> new IllegalArgumentException("No OntologicalField with type: " + type)
		);
	}

	OntologicalField(final String type) {
		this.type = type;
	}
}
