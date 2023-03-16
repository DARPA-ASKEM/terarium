package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum ProvenanceType {
	@JsonAlias("Dataset")
	DATASET("Dataset"),
	@JsonAlias("Intermediate")
	INTERMEDIATE("Intermediate"),
	@JsonAlias("Model")
	MODEL("Model"),
	@JsonAlias("ModelParameter")
	MODEL_PARAMETER("ModelParameter"),
	@JsonAlias("ModelRevision")
	MODEL_REVISION("ModelRevision"),
	@JsonAlias("Plan")
	PLAN("Plan"),
	@JsonAlias("PlanParameter")
	PLAN_PARAMETER("PlanParameter"),
	@JsonAlias("Publication")
	PUBLICATION("Publication"),
	@JsonAlias("Project")
	PROJECT("Project"),
	@JsonAlias("Concept")
	CONCEPT("Concept"),
	@JsonAlias("SimulationRun")
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

	@Override
	@JsonValue
	public String toString() {
		return type;
	}

	ProvenanceType(final String type) {
		this.type = type;
	}
}
