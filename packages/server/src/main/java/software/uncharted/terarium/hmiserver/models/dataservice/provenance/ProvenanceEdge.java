package software.uncharted.terarium.hmiserver.models.dataservice.provenance;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.data.neo4j.core.schema.Id;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ProvenanceEdge implements Serializable {

	private ProvenanceRelationType relationType;

	private ProvenanceNode left;

	private ProvenanceNode right;
}
