package software.uncharted.terarium.hmiserver.models.dataservice.provenance;

import java.util.Arrays;

public enum ProvenanceRelationType {
	BEGINS_AT("BEGINS_AT"),
	CITES("CITES"),
	COMBINED_FROM("COMBINED_FROM"),
	CONTAINS("CONTAINS"),
	COPIED_FROM("COPIED_FROM"),
	DECOMPOSED_FROM("DECOMPOSED_FROM"),
	DERIVED_FROM("DERIVED_FROM"),
	EDITED_FROM("EDITED_FROM"),
	EQUIVALENT_OF("EQUIVALENT_OF"),
	EXTRACTED_FROM("EXTRACTED_FROM"),
	GENERATED_BY("GENERATED_BY"),
	GLUED_FROM("GLUED_FROM"),
	IS_CONCEPT_OF("IS_CONCEPT_OF"),
	PARAMETER_OF("PARAMETER_OF"),
	REINTERPRETS("REINTERPRETS"),
	STRATIFIED_FROM("STRATIFIED_FROM"),
	USES("USES");

	public final String type;

	/**
	 * Returns the enum for a given string representation of a RelationType
	 *
	 * @param type the string representation of a RelationType
	 * @return a RelationType from the type string
	 * @throws IllegalArgumentException if the RelationType is not found
	 */
	public static ProvenanceRelationType findByType(final String type) {
		return Arrays.stream(values())
			.filter(value -> type.equalsIgnoreCase(value.type))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("No RelationType with type: " + type));
	}

	ProvenanceRelationType(final String type) {
		this.type = type;
	}
}
