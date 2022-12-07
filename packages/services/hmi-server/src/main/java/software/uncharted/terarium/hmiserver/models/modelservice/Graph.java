package software.uncharted.terarium.hmiserver.models.modelservice;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
public class Graph implements Serializable {
	private List<Node> nodes;
	private List<Edge> edges;
}
