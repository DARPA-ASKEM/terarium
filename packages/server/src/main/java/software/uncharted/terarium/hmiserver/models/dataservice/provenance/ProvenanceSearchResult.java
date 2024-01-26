package software.uncharted.terarium.hmiserver.models.dataservice.provenance;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
public class ProvenanceSearchResult implements Serializable {

	private List<ProvenanceNode> nodes;
	private List<ProvenanceEdge> edges;
}
