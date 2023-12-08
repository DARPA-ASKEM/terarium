package software.uncharted.terarium.hmiserver.models.dataservice.provenance;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ProvenanceSearchResult implements Serializable {

	private List<ProvenanceNode> nodes;
	private List<ProvenanceEdge> edges;
}
