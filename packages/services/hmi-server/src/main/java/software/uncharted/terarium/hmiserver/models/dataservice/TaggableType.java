package software.uncharted.terarium.hmiserver.models.dataservice;

public enum TaggableType {
	DATASETS("datasets"),
	FEATURES("features"),
	MODELS("models"),
	PROJECTS("projects"),
	SIMULATION_PLAN("simulation_plan");

	public final String type;

	TaggableType(final String type) {
		this.type = type;
	}
}
