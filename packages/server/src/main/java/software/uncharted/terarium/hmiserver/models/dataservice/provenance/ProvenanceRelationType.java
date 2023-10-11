package software.uncharted.terarium.hmiserver.models.dataservice.provenance;
import java.util.Arrays;
public enum ProvenanceRelationType {
	CITES("cites"),
	COPIED_FROM("copiedfrom"),
	DERIVED_FROM("derivedfrom"),
	EDITED_FROM("editedFrom"),
	GLUED_FROM("gluedFrom"),
	STRATIFIED_FROM("stratifiedFrom"),
	EXTRACTED_FROM("EXTRACTED_FROM");

	public final String type;

	/**
	 * Returns the enum for a given string representation of a RelationType
	 *
	 * @param type the string representation of a RelationType
	 * @return a RelationType from the type string
	 * @throws IllegalArgumentException if the RelationType is not found
	 */
	public static ProvenanceRelationType findByType(final String type) {
		return Arrays.stream(values()).filter(
			value -> type.equalsIgnoreCase(value.type)).findFirst().orElseThrow(() -> new IllegalArgumentException("No RelationType with type: " + type)
		);
	}

	ProvenanceRelationType(final String type) {
		this.type = type;
	}

	
}