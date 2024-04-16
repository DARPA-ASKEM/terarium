package software.uncharted.terarium.hmiserver.models.simulationservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@TSModel
public class CiemssStatusUpdate {
	private Number loss;

	private Number progress;

	@JsonAlias("job_id")
	private String jobId;
}
