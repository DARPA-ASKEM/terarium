package software.uncharted.terarium.hmiserver.models.dataservice.provenance;

import java.io.Serializable;
import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.neo4j.core.schema.Id;

@Data
@Accessors(chain = true)
public class ProvenanceEdge implements Serializable {

  @Id private UUID id;

  private ProvenanceRelationType relationType;

  private ProvenanceNode left;

  private ProvenanceNode right;
}
