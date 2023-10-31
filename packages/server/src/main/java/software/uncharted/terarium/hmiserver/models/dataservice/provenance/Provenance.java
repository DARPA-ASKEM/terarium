package software.uncharted.terarium.hmiserver.models.dataservice.provenance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class Provenance implements Serializable {

	private String id;

	private String timestamp;

	@JsonProperty("relation_type")
	private ProvenanceRelationType relationType;

	private String left;

	@JsonProperty("left_type")
	private ProvenanceType leftType;

	private String right;

	@JsonProperty("right_type")
	private ProvenanceType rightType;

	@JsonProperty("user_id")
	private String userId;

	public Provenance(ProvenanceRelationType extractedFrom, String left, ProvenanceType leftType, String right, ProvenanceType rightType){
		this.relationType = extractedFrom;
		this.left = left;
		this.leftType = leftType;
		this.right = right;
		this.rightType = rightType;
	}
}
