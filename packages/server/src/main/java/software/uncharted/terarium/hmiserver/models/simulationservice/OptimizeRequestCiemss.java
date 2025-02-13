package software.uncharted.terarium.hmiserver.models.simulationservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.simulationservice.interventions.Intervention;
import software.uncharted.terarium.hmiserver.models.simulationservice.parts.OptimizeExtra;
import software.uncharted.terarium.hmiserver.models.simulationservice.parts.OptimizeInterventions;
import software.uncharted.terarium.hmiserver.models.simulationservice.parts.OptimizeQoi;
import software.uncharted.terarium.hmiserver.models.simulationservice.parts.TimeSpan;

@Data
@Accessors(chain = true)
@TSModel
// Used to kick off a Optimize job in simulation-service
public class OptimizeRequestCiemss implements Serializable {

	@JsonAlias("model_config_id")
	private UUID modelConfigId;

	private TimeSpan timespan;

	// https://github.com/DARPA-ASKEM/pyciemss-service/blob/main/service/models/operations/optimize.py#L80
	private List<OptimizeInterventions> optimizeInterventions;

	@TSOptional
	@JsonAlias("fixed_interventions")
	private List<Intervention> fixedInterventions;

	@JsonAlias("logging_step_size")
	@TSOptional
	private Double loggingStepSize;

	private List<OptimizeQoi> qoi;

	private OptimizeExtra extra;
	private String engine;

	@JsonAlias("user_id")
	private String userId;
}
