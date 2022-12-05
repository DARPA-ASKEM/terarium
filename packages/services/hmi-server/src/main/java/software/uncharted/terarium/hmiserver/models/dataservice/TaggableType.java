package software.uncharted.terarium.hmiserver.models.dataservice;

import java.util.Arrays;

public enum TaggableType {
	DATASETS("datasets"),
	FEATURES("features"),
	INTERMEDIATES("intermediates"),
	MODEL_PARAMETERS("model_parameters"),
	MODELS("models"),
	PROJECTS("projects"),
	PUBLICATIONS("publications"),
	QUALIFIERS("qualifiers"),
	SIMULATION_PARAMETERS("simulation_parameters"),
	SIMULATION_PLANS("simulation_plans"),
	SIMULATION_RUNS("simulation_runs");

	public final String type;

	/**
	 * Returns the enum name for a given value of TaggableType
	 *
	 * @param type the TaggableType to attempt to find the name for.
	 * @return a String representation of the TaggableType name
	 * @throws IllegalArgumentException if the TaggableType is not found
	 */
	public static String findByType(final String type) {

		return Arrays.stream(values()).filter(
			value -> type.equalsIgnoreCase(value.type)).findFirst().orElseThrow(() -> new IllegalArgumentException("No TaggableType with type: " + type)
		).name();

	}

	TaggableType(final String type) {
		this.type = type;
	}
}
