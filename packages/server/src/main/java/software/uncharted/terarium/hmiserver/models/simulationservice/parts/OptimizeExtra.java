package software.uncharted.terarium.hmiserver.models.simulationservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import java.io.Serializable;

@Data
@Accessors(chain = true)
@TSModel
// Used to kick off a Optimize job in simulation-service
public class OptimizeExtra implements Serializable {
	@JsonAlias("num_samples")
	private int numSamples;
	@JsonAlias("inferred_parameters")
	@TSOptional
	private String inferredParameters;
	private int maxiter;
	private int maxfeval;

}
