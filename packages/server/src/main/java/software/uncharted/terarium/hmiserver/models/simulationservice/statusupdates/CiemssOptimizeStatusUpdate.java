package software.uncharted.terarium.hmiserver.models.simulationservice.statusupdates;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@TSModel
public class CiemssOptimizeStatusUpdate extends CiemssStatusUpdate {

	@JsonAlias("current_results")
	private List<Number> currentResults;

	@JsonAlias("total_possible_iterations")
	private Number totalPossibleIterations;

	@JsonIgnore
	public JsonNode getDataToPersist() {
		final ObjectMapper mapper = new ObjectMapper();
		return mapper.valueToTree(this);
	}
}
