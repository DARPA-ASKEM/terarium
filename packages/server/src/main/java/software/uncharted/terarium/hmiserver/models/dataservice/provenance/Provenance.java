package software.uncharted.terarium.hmiserver.models.dataservice.provenance;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

@Entity
@Data
@Accessors(chain = true)
public class Provenance implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@TSOptional
	private UUID id;

	@CreationTimestamp
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Timestamp createdOn;

	@UpdateTimestamp
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Timestamp updatedOn;

	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Timestamp deletedOn;

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
