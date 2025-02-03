package software.uncharted.terarium.hmiserver.models.simulationservice.parts;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

@Data
@Accessors(chain = true)
@TSModel
// Interventions applied by the user within the optimization box.
public class OptimizeInterventions {

	// This denotes whether the intervention is on a start date, or a parameter value.
	// https://github.com/DARPA-ASKEM/pyciemss-service/blob/main/service/models/operations/optimize.py#L99
	private String interventionType;

	@JsonAlias("param_name")
	private String paramName;

	@TSOptional
	@JsonAlias("param_value")
	private Double paramValue;

	@TSOptional
	@JsonAlias("start_time")
	private Integer startTime;

	@JsonAlias("time_objective_function")
	private String timeObjectiveFunction;

	@JsonAlias("parameter_objective_function")
	private String parameterObjectiveFunction;

	@TSOptional
	@JsonAlias("start_time_initial_guess")
	private Double startTimeInitialGuess;

	@TSOptional
	@JsonAlias("param_value_initial_guess")
	private Double paramValueInitialGuess;

	@JsonAlias("relative_importance")
	private Double relativeImportance;

	@Override
	public String toString() {
		return (" { Parameter Names: " + this.paramName + " start time: " + startTime.toString() + " } ");
	}
}
