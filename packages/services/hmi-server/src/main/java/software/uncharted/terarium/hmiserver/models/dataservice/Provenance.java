package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;
import java.time.Instant;

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

	RelationType(final String type) {
		this.type = type;
	}
}
