package software.uncharted.terarium.hmiserver.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.io.Serializable;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SimulationIntermediateResultsCiemss implements Serializable {

	@JsonAlias("job_id")
	private String jobId;

	private Double progress;

	@Override
	public String toString() {
		return "{job_id: '" + this.jobId + "', progress: " + Double.toString(this.progress) + "}";
	}
}
