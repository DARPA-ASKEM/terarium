package software.uncharted.terarium.hmiserver.models.dataservice.provenance;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.Accessors;

@Entity
@Data
@Accessors(chain = true)
public class Provenance implements Serializable {

	@Id
	private String id;

	private String timestamp;

	private String concept;

	@JsonProperty("relation_type")
	private ProvenanceRelationType relationType;

	@Column(name = "left_node")
	private String left;

	@JsonProperty("left_type")
	private ProvenanceType leftType;

	@Column(name = "right_node")
	private String right;

	@JsonProperty("right_type")
	private ProvenanceType rightType;

	@JsonProperty("user_id")
	private String userId;

	public Provenance() {
	}

	public Provenance(
			ProvenanceRelationType extractedFrom,
			String left,
			ProvenanceType leftType,
			String right,
			ProvenanceType rightType) {
		this.relationType = extractedFrom;
		this.left = left;
		this.leftType = leftType;
		this.right = right;
		this.rightType = rightType;
	}
}
