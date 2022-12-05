package software.uncharted.terarium.hmiserver.models.dataservice;

import java.util.Arrays;

public enum ResourceType {
	DATASETS("datasets"),
	EXTRACTIONS("extractions"),
	INTERMEDIATES("intermediates"),
	MODELS("models"),
	PLANS("plans"),
	PUBLICATIONS("publications"),
	SIMULATION_RUNS("simulation_runs");

	public final String type;

	/**
	 * Returns the enum name for a given value of ResourceType
	 *
	 * @param type the ResourceType to attempt to find the name for.
	 * @return a String representation of the ResourceType name
	 * @throws IllegalArgumentException if the ResourceType is not found
	 */
	public static String findByType(final String type) {

		return Arrays.stream(values()).filter(
			value -> type.equalsIgnoreCase(value.type)).findFirst().orElseThrow(() -> new IllegalArgumentException("No ResourceType with type: " + type)
		).name();

	}

	ResourceType(final String type) {
		this.type = type;
	}
}
