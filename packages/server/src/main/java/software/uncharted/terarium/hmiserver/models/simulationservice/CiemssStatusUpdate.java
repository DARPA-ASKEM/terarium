package software.uncharted.terarium.hmiserver.models.simulationservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

@Data
@TSModel
public class CiemssStatusUpdate {

	@JsonAlias("job_id")
	private String jobId;

	private Number progress;

	@TSOptional
	private Number loss;

	@TSOptional
	@JsonAlias("current_results")
	private List<Number> currentResults;

	@TSOptional
	@JsonAlias("total_possible_iterations")
	private Number totalPossibleIterations;

	@JsonIgnore
	public JsonNode getDataToPersist() {
		final ObjectMapper mapper = new ObjectMapper();
		return mapper.valueToTree(this);
	}
}
