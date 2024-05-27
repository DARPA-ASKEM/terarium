package software.uncharted.terarium.hmiserver.models.simulationservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.simulationservice.parts.Intervention;
import software.uncharted.terarium.hmiserver.models.simulationservice.parts.OptimizeExtra;
import software.uncharted.terarium.hmiserver.models.simulationservice.parts.OptimizeQoi;
import software.uncharted.terarium.hmiserver.models.simulationservice.parts.OptimizedInterventions;
import software.uncharted.terarium.hmiserver.models.simulationservice.parts.TimeSpan;

@Data
@Accessors(chain = true)
@TSModel
// Used to kick off a Optimize job in simulation-service
public class OptimizeRequestCiemss implements Serializable {
	@JsonAlias("model_config_id")
	private UUID modelConfigId;

	private TimeSpan timespan;

	@TSOptional
	// https://github.com/DARPA-ASKEM/pyciemss-service/blob/main/service/models/operations/optimize.py#L80
	private OptimizedInterventions policyInterventions;

	@TSOptional
	// The interventions provided via the model config which are not being optimized on
	private List<Intervention> fixedStaticParameterInterventions;

	@JsonAlias("step_size")
	@TSOptional
	private Double stepSize;

	private OptimizeQoi qoi;

	@JsonAlias("risk_bound")
	private Double riskBound;

	@JsonAlias("initial_guess_interventions")
	private List<Double> initialGuessInterventions;

	@JsonAlias("bounds_interventions")
	private List<List<Double>> boundsInterventions;

	private OptimizeExtra extra;
	private String engine;

	@JsonAlias("user_id")
	private String userId;
}
