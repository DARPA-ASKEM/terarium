package software.uncharted.terarium.hmiserver.models;

import lombok.Data;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonAlias;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class SimulationIntermediateResults implements Serializable {
	@JsonAlias("job_id")
	private String jobId;
	private float progress;
}
