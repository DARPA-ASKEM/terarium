package software.uncharted.terarium.hmiserver.models.dataservice.provenance;

import java.io.Serializable;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ProvenanceEdge implements Serializable {

    private ProvenanceRelationType relationType;

    private ProvenanceNode left;

    private ProvenanceNode right;
}
