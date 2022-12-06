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
	 * Returns the enum for a given string representation of a ResourceType
	 *
	 * @param type the string representation of a ResourceType
	 * @return a ResourceType from the type string
	 * @throws IllegalArgumentException if the ResourceType is not found
	 */
	public static ResourceType findByType(final String type) {
		return Arrays.stream(values()).filter(
			value -> type.equalsIgnoreCase(value.type)).findFirst().orElseThrow(() -> new IllegalArgumentException("No ResourceType with type: " + type)
		);
	}

	ResourceType(final String type) {
		this.type = type;
	}
}
