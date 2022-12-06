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
	 * Returns the enum for a given string representation of a TaggableType
	 *
	 * @param type the string representation of a TaggableType
	 * @return a TaggableType from the type string
	 * @throws IllegalArgumentException if the TaggableType is not found
	 */
	public static TaggableType findByType(final String type) {
		return Arrays.stream(values()).filter(
			value -> type.equalsIgnoreCase(value.type)).findFirst().orElseThrow(() -> new IllegalArgumentException("No TaggableType with type: " + type)
		);
	}

	TaggableType(final String type) {
		this.type = type;
	}
}
