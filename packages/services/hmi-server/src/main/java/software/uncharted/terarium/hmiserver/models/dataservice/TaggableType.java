package software.uncharted.terarium.hmiserver.models.dataservice;

public enum TaggableType {
	DATASETS("datasets"),
	FEATURES("features"),
	INTERMEDIATES("intermediates"),
	MODEL_PARAMETERS("model_parameters"),
	MODELS("models"),
	PROJECTS("projects"),
	PUBLICATIONS("publications"),
	QUALIFIERS("qualifiers"),
	SIMULATION_PARAMETERS("simulation_parameters"),
	SIMULATION_PLANS("simulation_plans"),
	SIMULATION_RUNS("simulation_runs");

	public final String type;

	TaggableType(final String type) {
		this.type = type;
	}
}
