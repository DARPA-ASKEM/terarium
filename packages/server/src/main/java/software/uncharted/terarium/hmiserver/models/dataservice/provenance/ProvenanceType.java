package software.uncharted.terarium.hmiserver.models.dataservice.provenance;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum ProvenanceType {
	@JsonAlias("Concept")
	CONCEPT("Concept"),

	@JsonAlias("Dataset")
	DATASET("Dataset"),

	@JsonAlias("Model")
	MODEL("Model"),

	@JsonAlias("ModelRevision")
	MODEL_REVISION("ModelRevision"),

	@JsonAlias("ModelConfiguration")
	MODEL_CONFIGURATION("ModelConfiguration"),

	@JsonAlias("Project")
	PROJECT("Project"),

	@JsonAlias("Simulation")
	SIMULATION("Simulation"),

	@JsonAlias("SimulationRun")
	SIMULATION_RUN("SimulationRun"),

	@JsonAlias("Plan")
	PLAN("Plan"),

	@JsonAlias("Artifact")
	ARTIFACT("Artifact"),

	@JsonAlias("Code")
	CODE("Code"),

	@JsonAlias("Document")
	DOCUMENT("Document"),

	@JsonAlias("Workflow")
	WORKFLOW("Workflow"),

	@JsonAlias("Equation")
	EQUATION("Equation"),

	@JsonAlias("InterventionPolicy")
	INTERVENTION_POLICY("InterventionPolicy");

	public final String type;

	/**
	 * Returns the enum for a given string representation of a ProvenanceType
	 *
	 * @param type the string representation of a ProvenanceType
	 * @return a ProvenanceType from the type string
	 * @throws IllegalArgumentException if the ProvenanceType is not found
	 */
	public static ProvenanceType findByType(final String type) {
		return Arrays.stream(values())
			.filter(value -> type.equalsIgnoreCase(value.type))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("No ProvenanceType with type: " + type));
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
