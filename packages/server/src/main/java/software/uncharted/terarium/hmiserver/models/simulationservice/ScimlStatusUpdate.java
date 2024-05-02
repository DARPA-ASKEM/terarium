package software.uncharted.terarium.hmiserver.models.simulationservice;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@TSModel
public class ScimlStatusUpdate {

	// Error or loss after each iteration
	private Number loss;

	// Current iteration
	private Number iter;

	// New values calulated after each iteration
	private Map<String, Number> params;

	// The simulation id associated with this run
	private String id;

	// New state variables calulated after each iteration
	@JsonAlias("sol_data")
	private Map<String, JsonNode> solData;

	// List of timesteps
	private List<Number> timesteps;

	@JsonIgnore
	public JsonNode getDataToPersist() {
		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode jsonNode = mapper.valueToTree(this);
		((ObjectNode) jsonNode).remove("sol_data"); // remove large field
		return jsonNode;
	}
}
