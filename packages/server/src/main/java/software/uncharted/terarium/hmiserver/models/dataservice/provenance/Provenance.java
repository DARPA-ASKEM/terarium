package software.uncharted.terarium.hmiserver.models.dataservice.provenance;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@TSModel
public class Provenance implements Serializable {

	@Serial
	private static final long serialVersionUID = 8443258388173011137L;

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private UUID id;

	@TSOptional
	@CreationTimestamp
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Timestamp createdOn;

	@TSOptional
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
		final ProvenanceRelationType extractedFrom,
		final String left,
		final ProvenanceType leftType,
		final String right,
		final ProvenanceType rightType) {
		this.relationType = extractedFrom;
		this.left = left;
		this.leftType = leftType;
		this.right = right;
		this.rightType = rightType;
	}
}
