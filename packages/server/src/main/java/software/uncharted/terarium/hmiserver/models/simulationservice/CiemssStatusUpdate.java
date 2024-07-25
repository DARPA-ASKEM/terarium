package software.uncharted.terarium.hmiserver.models.simulationservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@TSModel
public class CiemssStatusUpdate {

	private Number loss;

	private Number progress;

	@JsonAlias("job_id")
	private String jobId;

	@JsonIgnore
	public JsonNode getDataToPersist() {
		final ObjectMapper mapper = new ObjectMapper();
		return mapper.valueToTree(this);
	}
}
