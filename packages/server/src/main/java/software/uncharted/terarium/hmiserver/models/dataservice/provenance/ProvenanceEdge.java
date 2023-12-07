package software.uncharted.terarium.hmiserver.models.dataservice.provenance;

import java.io.Serializable;

import org.springframework.data.neo4j.core.schema.Id;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ProvenanceEdge implements Serializable {

	@Id
	private String id;

	private ProvenanceRelationType relationType;

	private ProvenanceNode left;

	private ProvenanceNode right;
}
