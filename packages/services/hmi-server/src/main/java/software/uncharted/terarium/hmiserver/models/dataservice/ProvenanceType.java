package software.uncharted.terarium.hmiserver.models.dataservice;

import java.util.Arrays;

public enum ProvenanceType {
	DATASET("Dataset"),
	INTERMEDIATE("Intermediate"),
	MODEL("Model"),
	MODEL_PARAMETER("ModelParameter"),
	MODEL_REVISION("ModelRevision"),
	PLAN("Plan"),
	PLAN_PARAMETER("PlanParameter"),
	PUBLICATION("Publication"),
	PROJECT("Project"),
	CONCEPT("Concept"),
	SIMULATION_RUN("SimulationRun");

	public final String type;

	/**
	 * Returns the enum for a given string representation of a ProvenanceType
	 *
	 * @param type the string representation of a ProvenanceType
	 * @return a ProvenanceType from the type string
	 * @throws IllegalArgumentException if the ProvenanceType is not found
	 */
	public static ProvenanceType findByType(final String type) {
		return Arrays.stream(values()).filter(
			value -> type.equalsIgnoreCase(value.type)).findFirst().orElseThrow(() -> new IllegalArgumentException("No ProvenanceType with type: " + type)
		);
	}

	ProvenanceType(final String type) {
		this.type = type;
	}
}
