package software.uncharted.terarium.hmiserver.models.simulationservice.statusupdates;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@TSModel
public abstract class CiemssStatusUpdate {

	@JsonAlias("job_id")
	private String jobId;

	private Number progress;

	private CiemssStatusType type;

	@JsonIgnore
	public JsonNode getDataToPersist() {
		final ObjectMapper mapper = new ObjectMapper();
		return mapper.valueToTree(this);
	}
}
