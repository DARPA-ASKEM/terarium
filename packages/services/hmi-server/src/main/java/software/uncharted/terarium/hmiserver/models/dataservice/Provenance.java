package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;
import java.time.Instant;
import java.util.Arrays;

@Data
@Accessors(chain = true)
public class Provenance implements Serializable {

	private String id;

	private Instant timestamp;

	@JsonbProperty("relation_type")
	private RelationType relationType;

	private String left;

	@JsonbProperty("left_type")
	private ResourceType leftType;

	private String right;

	@JsonbProperty("right_type")
	private ResourceType rightType;

	@JsonbProperty("user_id")
	private String userId;
}

enum RelationType {
	CITES("cites"),
	COPIED_FROM("copiedfrom"),
	DERIVED_FROM("derivedfrom"),
	EDITED_FROM("editedFrom"),
	GLUED_FROM("gluedFrom"),
	STRATIFIED_FROM("stratifiedFrom");

	public final String type;

	/**
	 * Returns the enum for a given string representation of a RelationType
	 *
	 * @param type the string representation of a RelationType
	 * @return a RelationType from the type string
	 * @throws IllegalArgumentException if the RelationType is not found
	 */
	public static RelationType findByType(final String type) {
		return Arrays.stream(values()).filter(
			value -> type.equalsIgnoreCase(value.type)).findFirst().orElseThrow(() -> new IllegalArgumentException("No RelationType with type: " + type)
		);
	}

	RelationType(final String type) {
		this.type = type;
	}
}
