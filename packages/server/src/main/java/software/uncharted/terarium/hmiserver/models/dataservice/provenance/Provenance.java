package software.uncharted.terarium.hmiserver.models.dataservice.provenance;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.persistence.Column;
import java.io.Serial;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TSModel
public class Provenance extends TerariumAsset {

	@Serial
	private static final long serialVersionUID = 8443258388173011137L;

	private String concept;

	@JsonAlias("relation_type")
	private ProvenanceRelationType relationType;

	@Column(name = "left_node")
	private UUID left;

	@JsonAlias("left_type")
	private ProvenanceType leftType;

	@Column(name = "right_node")
	private UUID right;

	@JsonAlias("right_type")
	private ProvenanceType rightType;

	@JsonAlias("user_id")
	private String userId;

	public Provenance() {}

	public Provenance(
		final ProvenanceRelationType extractedFrom,
		final UUID left,
		final ProvenanceType leftType,
		final UUID right,
		final ProvenanceType rightType
	) {
		this.relationType = extractedFrom;
		this.left = left;
		this.leftType = leftType;
		this.right = right;
		this.rightType = rightType;
	}
}
