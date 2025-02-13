package software.uncharted.terarium.hmiserver.models.simulationservice.parts;

import com.fasterxml.jackson.annotation.JsonAlias;
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

	@JsonAlias("relative_importance")
	private Double relativeImportance;

	@TSOptional
	@JsonAlias("param_value_initial_guess")
	private Double paramValueInitialGuess;

	@JsonAlias("parameter_value_lower_bound")
	private Double parameterValueLowerBound;

	@JsonAlias("parameter_value_upper_bound")
	private Double parameterValueUpperBound;

	@TSOptional
	@JsonAlias("start_time_initial_guess")
	private Double startTimeInitialGuess;

	@JsonAlias("start_time_lower_bound")
	private Double startTimeLowerBound;

	@JsonAlias("start_time_upper_bound")
	private Double startTimeUpperBound;

	@Override
	public String toString() {
		return (" { Parameter Names: " + this.paramName + " start time: " + startTime.toString() + " } ");
	}
}
