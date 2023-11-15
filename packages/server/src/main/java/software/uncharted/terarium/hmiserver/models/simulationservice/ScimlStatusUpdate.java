package software.uncharted.terarium.hmiserver.models.simulationservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

import java.util.List;
import java.util.Map;

@Data
@TSModel
public class ScimlStatusUpdate {

	/** Error or loss after each iteration **/
	private Number loss;

	/** Current iteration **/
	private Number iter;

	/** New values calulated after each iteration **/
	private Map<String, Number> params;

	/** The simulation id associated with this run **/
	private String id;

	/** New state variables calulated after each iteration **/
	@JsonAlias("sol_data")
	private Map<String, JsonNode> solData;

	/** List of timesteps **/
	private List<Number> timesteps;
}
