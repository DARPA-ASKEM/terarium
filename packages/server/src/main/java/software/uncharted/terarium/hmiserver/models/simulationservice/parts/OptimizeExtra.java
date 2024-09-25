package software.uncharted.terarium.hmiserver.models.simulationservice.parts;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

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

	@TSOptional
	private int maxiter;

	@TSOptional
	private int maxfeval;

	@JsonAlias("is_minimized")
	@TSOptional
	private Boolean isMinimized;

	@TSOptional
	private List<Double> alpha;

	@JsonAlias("solver_method")
	@TSOptional
	private String solverMethod;

	@JsonAlias("solver_step_size")
	@TSOptional
	private Double solverStepSize;
}
