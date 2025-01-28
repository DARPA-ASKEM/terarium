package software.uncharted.terarium.hmiserver.models.dataservice.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class WorkflowPositions implements Serializable {

	@Data
	class Position {

		private Double x;
		private Double y;
	}

	private Map<String, Position> nodes;
	private Map<String, List<Position>> edges;
}
